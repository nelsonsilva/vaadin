/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.VScrollTable.VScrollTableBody.VScrollTableRow;

/**
 * VScrollTable
 * 
 * VScrollTable is a FlowPanel having two widgets in it: * TableHead component *
 * ScrollPanel
 * 
 * TableHead contains table's header and widgets + logic for resizing,
 * reordering and hiding columns.
 * 
 * ScrollPanel contains VScrollTableBody object which handles content. To save
 * some bandwidth and to improve clients responsiveness with loads of data, in
 * VScrollTableBody all rows are not necessary rendered. There are "spacers" in
 * VScrollTableBody to use the exact same space as non-rendered rows would use.
 * This way we can use seamlessly traditional scrollbars and scrolling to fetch
 * more rows instead of "paging".
 * 
 * In VScrollTable we listen to scroll events. On horizontal scrolling we also
 * update TableHeads scroll position which has its scrollbars hidden. On
 * vertical scroll events we will check if we are reaching the end of area where
 * we have rows rendered and
 * 
 * TODO implement unregistering for child components in Cells
 */
public class VScrollTable extends FlowPanel implements Table, ScrollHandler {

    public static final String CLASSNAME = "v-table";
    public static final String ITEM_CLICK_EVENT_ID = "itemClick";

    private static final double CACHE_RATE_DEFAULT = 2;

    /**
     * multiple of pagelength which component will cache when requesting more
     * rows
     */
    private double cache_rate = CACHE_RATE_DEFAULT;
    /**
     * fraction of pageLenght which can be scrolled without making new request
     */
    private double cache_react_rate = 0.75 * cache_rate;

    public static final char ALIGN_CENTER = 'c';
    public static final char ALIGN_LEFT = 'b';
    public static final char ALIGN_RIGHT = 'e';
    private int firstRowInViewPort = 0;
    private int pageLength = 15;
    private int lastRequestedFirstvisible = 0; // to detect "serverside scroll"

    private boolean showRowHeaders = false;

    private String[] columnOrder;

    private ApplicationConnection client;
    private String paintableId;

    private boolean immediate;

    private int selectMode = Table.SELECT_MODE_NONE;

    private final HashSet<String> selectedRowKeys = new HashSet<String>();

    private boolean initializedAndAttached = false;

    /**
     * Flag to indicate if a column width recalculation is needed due update.
     */
    private boolean headerChangedDuringUpdate = false;

    private final TableHead tHead = new TableHead();

    private final ScrollPanel bodyContainer = new ScrollPanel();

    private int totalRows;

    private Set<String> collapsedColumns;

    private final RowRequestHandler rowRequestHandler;
    private VScrollTableBody scrollBody;
    private int firstvisible = 0;
    private boolean sortAscending;
    private String sortColumn;
    private boolean columnReordering;

    /**
     * This map contains captions and icon urls for actions like: * "33_c" ->
     * "Edit" * "33_i" -> "http://dom.com/edit.png"
     */
    private final HashMap<Object, String> actionMap = new HashMap<Object, String>();
    private String[] visibleColOrder;
    private boolean initialContentReceived = false;
    private Element scrollPositionElement;
    private boolean enabled;
    private boolean showColHeaders;

    /** flag to indicate that table body has changed */
    private boolean isNewBody = true;

    /*
     * Read from the "recalcWidths" -attribute. When it is true, the table will
     * recalculate the widths for columns - desirable in some cases. For #1983,
     * marked experimental.
     */
    boolean recalcWidths = false;

    private final ArrayList<Panel> lazyUnregistryBag = new ArrayList<Panel>();
    private String height;
    private String width = "";
    private boolean rendering = false;

    public VScrollTable() {
        bodyContainer.addScrollHandler(this);
        bodyContainer.setStyleName(CLASSNAME + "-body");

        setStyleName(CLASSNAME);
        add(tHead);
        add(bodyContainer);

        rowRequestHandler = new RowRequestHandler();

    }

    @SuppressWarnings("unchecked")
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        rendering = true;
        if (client.updateComponent(this, uidl, true)) {
            rendering = false;
            return;
        }

        // we may have pending cache row fetch, cancel it. See #2136
        rowRequestHandler.cancel();

        enabled = !uidl.hasAttribute("disabled");

        this.client = client;
        paintableId = uidl.getStringAttribute("id");
        immediate = uidl.getBooleanAttribute("immediate");
        final int newTotalRows = uidl.getIntAttribute("totalrows");
        if (newTotalRows != totalRows) {
            if (scrollBody != null) {
                if (totalRows == 0) {
                    tHead.clear();
                }
                initializedAndAttached = false;
                initialContentReceived = false;
                isNewBody = true;
            }
            totalRows = newTotalRows;
        }

        setCacheRate(uidl.hasAttribute("cr") ? uidl.getDoubleAttribute("cr")
                : CACHE_RATE_DEFAULT);

        recalcWidths = uidl.hasAttribute("recalcWidths");
        if (recalcWidths) {
            tHead.clear();
        }

        if (uidl.hasAttribute("pagelength")) {
            pageLength = uidl.getIntAttribute("pagelength");
        } else {
            // pagelenght is "0" meaning scrolling is turned off
            pageLength = totalRows;
        }
        firstvisible = uidl.hasVariable("firstvisible") ? uidl
                .getIntVariable("firstvisible") : 0;
        if (firstvisible != lastRequestedFirstvisible && scrollBody != null) {
            // received 'surprising' firstvisible from server: scroll there
            firstRowInViewPort = firstvisible;
            bodyContainer.setScrollPosition((int) (firstvisible * scrollBody
                    .getRowHeight()));
        }

        showRowHeaders = uidl.getBooleanAttribute("rowheaders");
        showColHeaders = uidl.getBooleanAttribute("colheaders");

        if (uidl.hasVariable("sortascending")) {
            sortAscending = uidl.getBooleanVariable("sortascending");
            sortColumn = uidl.getStringVariable("sortcolumn");
        }

        if (uidl.hasVariable("selected")) {
            final Set<String> selectedKeys = uidl
                    .getStringArrayVariableAsSet("selected");
            selectedRowKeys.clear();
            for (String string : selectedKeys) {
                selectedRowKeys.add(string);
            }
        }

        if (uidl.hasAttribute("selectmode")) {
            if (uidl.getBooleanAttribute("readonly")) {
                selectMode = Table.SELECT_MODE_NONE;
            } else if (uidl.getStringAttribute("selectmode").equals("multi")) {
                selectMode = Table.SELECT_MODE_MULTI;
            } else if (uidl.getStringAttribute("selectmode").equals("single")) {
                selectMode = Table.SELECT_MODE_SINGLE;
            } else {
                selectMode = Table.SELECT_MODE_NONE;
            }
        }

        if (uidl.hasVariable("columnorder")) {
            columnReordering = true;
            columnOrder = uidl.getStringArrayVariable("columnorder");
        }

        if (uidl.hasVariable("collapsedcolumns")) {
            tHead.setColumnCollapsingAllowed(true);
            collapsedColumns = uidl
                    .getStringArrayVariableAsSet("collapsedcolumns");
        } else {
            tHead.setColumnCollapsingAllowed(false);
        }

        UIDL rowData = null;
        for (final Iterator it = uidl.getChildIterator(); it.hasNext();) {
            final UIDL c = (UIDL) it.next();
            if (c.getTag().equals("rows")) {
                rowData = c;
            } else if (c.getTag().equals("actions")) {
                updateActionMap(c);
            } else if (c.getTag().equals("visiblecolumns")) {
                tHead.updateCellsFromUIDL(c);
            }
        }
        updateHeader(uidl.getStringArrayAttribute("vcolorder"));

        if (!recalcWidths && initializedAndAttached) {
            updateBody(rowData, uidl.getIntAttribute("firstrow"), uidl
                    .getIntAttribute("rows"));
            if (headerChangedDuringUpdate) {
                lazyAdjustColumnWidths.schedule(1);
            } else {
                // webkits may still bug with their disturbing scrollbar bug,
                // See #3457
                // run overflow fix for scrollable area
                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        Util.runWebkitOverflowAutoFix(bodyContainer
                                .getElement());
                    }
                });
            }
        } else {
            if (scrollBody != null) {
                scrollBody.removeFromParent();
                lazyUnregistryBag.add(scrollBody);
            }
            scrollBody = new VScrollTableBody();

            scrollBody.renderInitialRows(rowData, uidl
                    .getIntAttribute("firstrow"), uidl.getIntAttribute("rows"));
            bodyContainer.add(scrollBody);
            initialContentReceived = true;
            ApplicationConnection.getConsole().log("foo");
            if (isAttached()) {
                ApplicationConnection.getConsole().log("bar");
                sizeInit();
            }
            scrollBody.restoreRowVisibility();
        }

        if (selectMode == Table.SELECT_MODE_NONE) {
            scrollBody.addStyleName(CLASSNAME + "-body-noselection");
        } else {
            scrollBody.removeStyleName(CLASSNAME + "-body-noselection");
        }

        hideScrollPositionAnnotation();
        purgeUnregistryBag();
        rendering = false;
        headerChangedDuringUpdate = false;
    }

    private void setCacheRate(double d) {
        if (cache_rate != d) {
            cache_rate = d;
            cache_react_rate = 0.75 * d;
        }
    }

    /**
     * Unregisters Paintables in "trashed" HasWidgets (IScrollTableBodys or
     * IScrollTableRows). This is done lazily as Table must survive from
     * "subtreecaching" logic.
     */
    private void purgeUnregistryBag() {
        for (Iterator<Panel> iterator = lazyUnregistryBag.iterator(); iterator
                .hasNext();) {
            client.unregisterChildPaintables(iterator.next());
        }
        lazyUnregistryBag.clear();
    }

    private void updateActionMap(UIDL c) {
        final Iterator<?> it = c.getChildIterator();
        while (it.hasNext()) {
            final UIDL action = (UIDL) it.next();
            final String key = action.getStringAttribute("key");
            final String caption = action.getStringAttribute("caption");
            actionMap.put(key + "_c", caption);
            if (action.hasAttribute("icon")) {
                // TODO need some uri handling ??
                actionMap.put(key + "_i", client.translateVaadinUri(action
                        .getStringAttribute("icon")));
            }
        }

    }

    public String getActionCaption(String actionKey) {
        return actionMap.get(actionKey + "_c");
    }

    public String getActionIcon(String actionKey) {
        return actionMap.get(actionKey + "_i");
    }

    private void updateHeader(String[] strings) {
        if (strings == null) {
            return;
        }

        int visibleCols = strings.length;
        int colIndex = 0;
        if (showRowHeaders) {
            tHead.enableColumn("0", colIndex);
            visibleCols++;
            visibleColOrder = new String[visibleCols];
            visibleColOrder[colIndex] = "0";
            colIndex++;
        } else {
            visibleColOrder = new String[visibleCols];
            tHead.removeCell("0");
        }

        int i;
        for (i = 0; i < strings.length; i++) {
            final String cid = strings[i];
            visibleColOrder[colIndex] = cid;
            tHead.enableColumn(cid, colIndex);
            colIndex++;
        }

        tHead.setVisible(showColHeaders);

    }

    /**
     * @param uidl
     *            which contains row data
     * @param firstRow
     *            first row in data set
     * @param reqRows
     *            amount of rows in data set
     */
    private void updateBody(UIDL uidl, int firstRow, int reqRows) {
        if (uidl == null || reqRows < 1) {
            // container is empty, remove possibly existing rows
            if (firstRow < 0) {
                while (scrollBody.getLastRendered() > scrollBody.firstRendered) {
                    scrollBody.unlinkRow(false);
                }
                scrollBody.unlinkRow(false);
            }
            return;
        }

        scrollBody.renderRows(uidl, firstRow, reqRows);

        final int optimalFirstRow = (int) (firstRowInViewPort - pageLength
                * cache_rate);
        boolean cont = true;
        while (cont && scrollBody.getLastRendered() > optimalFirstRow
                && scrollBody.getFirstRendered() < optimalFirstRow) {
            // client.console.log("removing row from start");
            cont = scrollBody.unlinkRow(true);
        }
        final int optimalLastRow = (int) (firstRowInViewPort + pageLength + pageLength
                * cache_rate);
        cont = true;
        while (cont && scrollBody.getLastRendered() > optimalLastRow) {
            // client.console.log("removing row from the end");
            cont = scrollBody.unlinkRow(false);
        }
        scrollBody.fixSpacers();

        scrollBody.restoreRowVisibility();
    }

    /**
     * Gives correct column index for given column key ("cid" in UIDL).
     * 
     * @param colKey
     * @return column index of visible columns, -1 if column not visible
     */
    private int getColIndexByKey(String colKey) {
        // return 0 if asked for rowHeaders
        if ("0".equals(colKey)) {
            return 0;
        }
        for (int i = 0; i < visibleColOrder.length; i++) {
            if (visibleColOrder[i].equals(colKey)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isCollapsedColumn(String colKey) {
        if (collapsedColumns == null) {
            return false;
        }
        if (collapsedColumns.contains(colKey)) {
            return true;
        }
        return false;
    }

    private String getColKeyByIndex(int index) {
        return tHead.getHeaderCell(index).getColKey();
    }

    private void setColWidth(int colIndex, int w, boolean isDefinedWidth) {
        final HeaderCell cell = tHead.getHeaderCell(colIndex);
        cell.setWidth(w, isDefinedWidth);
        scrollBody.setColWidth(colIndex, w);
    }

    private int getColWidth(String colKey) {
        return tHead.getHeaderCell(colKey).getWidth();
    }

    private VScrollTableRow getRenderedRowByKey(String key) {
        final Iterator<Widget> it = scrollBody.iterator();
        VScrollTableRow r = null;
        while (it.hasNext()) {
            r = (VScrollTableRow) it.next();
            if (r.getKey().equals(key)) {
                return r;
            }
        }
        return null;
    }

    private void reOrderColumn(String columnKey, int newIndex) {

        final int oldIndex = getColIndexByKey(columnKey);

        // Change header order
        tHead.moveCell(oldIndex, newIndex);

        // Change body order
        scrollBody.moveCol(oldIndex, newIndex);

        /*
         * Build new columnOrder and update it to server Note that columnOrder
         * also contains collapsed columns so we cannot directly build it from
         * cells vector Loop the old columnOrder and append in order to new
         * array unless on moved columnKey. On new index also put the moved key
         * i == index on columnOrder, j == index on newOrder
         */
        final String oldKeyOnNewIndex = visibleColOrder[newIndex];
        if (showRowHeaders) {
            newIndex--; // columnOrder don't have rowHeader
        }
        // add back hidden rows,
        for (int i = 0; i < columnOrder.length; i++) {
            if (columnOrder[i].equals(oldKeyOnNewIndex)) {
                break; // break loop at target
            }
            if (isCollapsedColumn(columnOrder[i])) {
                newIndex++;
            }
        }
        // finally we can build the new columnOrder for server
        final String[] newOrder = new String[columnOrder.length];
        for (int i = 0, j = 0; j < newOrder.length; i++) {
            if (j == newIndex) {
                newOrder[j] = columnKey;
                j++;
            }
            if (i == columnOrder.length) {
                break;
            }
            if (columnOrder[i].equals(columnKey)) {
                continue;
            }
            newOrder[j] = columnOrder[i];
            j++;
        }
        columnOrder = newOrder;
        // also update visibleColumnOrder
        int i = showRowHeaders ? 1 : 0;
        for (int j = 0; j < newOrder.length; j++) {
            final String cid = newOrder[j];
            if (!isCollapsedColumn(cid)) {
                visibleColOrder[i++] = cid;
            }
        }
        client.updateVariable(paintableId, "columnorder", columnOrder, false);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (initialContentReceived) {
            sizeInit();
        }
    }

    @Override
    protected void onDetach() {
        rowRequestHandler.cancel();
        super.onDetach();
        // ensure that scrollPosElement will be detached
        if (scrollPositionElement != null) {
            final Element parent = DOM.getParent(scrollPositionElement);
            if (parent != null) {
                DOM.removeChild(parent, scrollPositionElement);
            }
        }
    }

    /**
     * Run only once when component is attached and received its initial
     * content. This function : * Syncs headers and bodys "natural widths and
     * saves the values. * Sets proper width and height * Makes deferred request
     * to get some cache rows
     */
    private void sizeInit() {
        /*
         * We will use browsers table rendering algorithm to find proper column
         * widths. If content and header take less space than available, we will
         * divide extra space relatively to each column which has not width set.
         * 
         * Overflow pixels are added to last column.
         */

        Iterator<Widget> headCells = tHead.iterator();
        int i = 0;
        int totalExplicitColumnsWidths = 0;
        int total = 0;
        float expandRatioDivider = 0;

        final int[] widths = new int[tHead.visibleCells.size()];

        tHead.enableBrowserIntelligence();
        // first loop: collect natural widths
        while (headCells.hasNext()) {
            final HeaderCell hCell = (HeaderCell) headCells.next();
            int w = hCell.getWidth();
            if (hCell.isDefinedWidth()) {
                // server has defined column width explicitly
                totalExplicitColumnsWidths += w;
            } else {
                if (hCell.getExpandRatio() > 0) {
                    expandRatioDivider += hCell.getExpandRatio();
                    w = 0;
                } else {
                    // get and store greater of header width and column width,
                    // and
                    // store it as a minimumn natural col width
                    w = hCell.getNaturalColumnWidth(i);
                }
                hCell.setNaturalMinimumColumnWidth(w);
            }
            widths[i] = w;
            total += w;
            i++;
        }

        tHead.disableBrowserIntelligence();

        boolean willHaveScrollbarz = willHaveScrollbars();

        // fix "natural" width if width not set
        if (width == null || "".equals(width)) {
            int w = total;
            w += scrollBody.getCellExtraWidth() * visibleColOrder.length;
            if (willHaveScrollbarz) {
                w += Util.getNativeScrollbarSize();
            }
            setContentWidth(w);
        }

        int availW = scrollBody.getAvailableWidth();
        if (BrowserInfo.get().isIE()) {
            // Hey IE, are you really sure about this?
            availW = scrollBody.getAvailableWidth();
        }
        availW -= scrollBody.getCellExtraWidth() * visibleColOrder.length;

        if (willHaveScrollbarz) {
            availW -= Util.getNativeScrollbarSize();
        }

        // TODO refactor this code to be the same as in resize timer
        boolean needsReLayout = false;

        if (availW > total) {
            // natural size is smaller than available space
            final int extraSpace = availW - total;
            final int totalWidthR = total - totalExplicitColumnsWidths;
            needsReLayout = true;

            if (expandRatioDivider > 0) {
                // visible columns have some active expand ratios, excess
                // space is divided according to them
                headCells = tHead.iterator();
                i = 0;
                while (headCells.hasNext()) {
                    HeaderCell hCell = (HeaderCell) headCells.next();
                    if (hCell.getExpandRatio() > 0) {
                        int w = widths[i];
                        final int newSpace = (int) (extraSpace * (hCell
                                .getExpandRatio() / expandRatioDivider));
                        w += newSpace;
                        widths[i] = w;
                    }
                    i++;
                }
            } else if (totalWidthR > 0) {
                // no expand ratios defined, we will share extra space
                // relatively to "natural widths" among those without
                // explicit width
                headCells = tHead.iterator();
                i = 0;
                while (headCells.hasNext()) {
                    HeaderCell hCell = (HeaderCell) headCells.next();
                    if (!hCell.isDefinedWidth()) {
                        int w = widths[i];
                        final int newSpace = extraSpace * w / totalWidthR;
                        w += newSpace;
                        widths[i] = w;
                    }
                    i++;
                }
            }

        } else {
            // bodys size will be more than available and scrollbar will appear
        }

        // last loop: set possibly modified values or reset if new tBody
        i = 0;
        headCells = tHead.iterator();
        while (headCells.hasNext()) {
            final HeaderCell hCell = (HeaderCell) headCells.next();
            if (isNewBody || hCell.getWidth() == -1) {
                final int w = widths[i];
                setColWidth(i, w, false);
            }
            i++;
        }

        initializedAndAttached = true;

        if (needsReLayout) {
            scrollBody.reLayoutComponents();
        }

        updatePageLength();

        /*
         * Fix "natural" height if height is not set. This must be after width
         * fixing so the components' widths have been adjusted.
         */
        if (height == null || "".equals(height)) {
            /*
             * We must force an update of the row height as this point as it
             * might have been (incorrectly) calculated earlier
             */

            int bodyHeight;
            if (pageLength == totalRows) {
                /*
                 * A hack to support variable height rows when paging is off.
                 * Generally this is not supported by scrolltable. We want to
                 * show all rows so the bodyHeight should be equal to the table
                 * height.
                 */
                // int bodyHeight = scrollBody.getOffsetHeight();
                bodyHeight = scrollBody.getRequiredHeight();
            } else {
                bodyHeight = (int) Math.round(scrollBody.getRowHeight(true)
                        * pageLength);
            }
            boolean needsSpaceForHorizontalSrollbar = (total > availW);
            if (needsSpaceForHorizontalSrollbar) {
                bodyHeight += Util.getNativeScrollbarSize();
            }
            bodyContainer.setHeight(bodyHeight + "px");
            Util.runWebkitOverflowAutoFix(bodyContainer.getElement());
        }

        isNewBody = false;

        if (firstvisible > 0) {
            // Deferred due some Firefox oddities. IE & Safari could survive
            // without
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    bodyContainer
                            .setScrollPosition((int) (firstvisible * scrollBody
                                    .getRowHeight()));
                    firstRowInViewPort = firstvisible;
                }
            });
        }

        if (enabled) {
            // Do we need cache rows
            if (scrollBody.getLastRendered() + 1 < firstRowInViewPort
                    + pageLength + (int) cache_react_rate * pageLength) {
                if (totalRows - 1 > scrollBody.getLastRendered()) {
                    // fetch cache rows
                    int firstInNewSet = scrollBody.getLastRendered() + 1;
                    rowRequestHandler.setReqFirstRow(firstInNewSet);
                    int lastInNewSet = (int) (firstRowInViewPort + pageLength + cache_rate
                            * pageLength);
                    if (lastInNewSet > totalRows - 1) {
                        lastInNewSet = totalRows - 1;
                    }
                    rowRequestHandler.setReqRows(lastInNewSet - firstInNewSet
                            + 1);
                    rowRequestHandler.deferRowFetch(1);
                }
            }
        }
    }

    /**
     * Note, this method is not official api although declared as protected.
     * Extend at you own risk.
     * 
     * @return true if content area will have scrollbars visible.
     */
    protected boolean willHaveScrollbars() {
        if (!(height != null && !height.equals(""))) {
            if (pageLength < totalRows) {
                return true;
            }
        } else {
            int fakeheight = (int) Math.round(scrollBody.getRowHeight()
                    * totalRows);
            int availableHeight = bodyContainer.getElement().getPropertyInt(
                    "clientHeight");
            if (fakeheight > availableHeight) {
                return true;
            }
        }
        return false;
    }

    private void announceScrollPosition() {
        if (scrollPositionElement == null) {
            scrollPositionElement = DOM.createDiv();
            DOM.setElementProperty(scrollPositionElement, "className",
                    CLASSNAME + "-scrollposition");
            DOM
                    .setStyleAttribute(scrollPositionElement, "position",
                            "absolute");
            DOM.appendChild(getElement(), scrollPositionElement);
        }

        DOM.setStyleAttribute(scrollPositionElement, "marginLeft", (DOM
                .getElementPropertyInt(getElement(), "offsetWidth") / 2 - 80)
                + "px");
        DOM.setStyleAttribute(scrollPositionElement, "marginTop", -(DOM
                .getElementPropertyInt(bodyContainer.getElement(),
                        "offsetHeight"))
                + "px");

        // indexes go from 1-totalRows, as rowheaders in index-mode indicate
        int last = (firstRowInViewPort + pageLength);
        if (last > totalRows) {
            last = totalRows;
        }
        DOM.setInnerHTML(scrollPositionElement, "<span>"
                + (firstRowInViewPort + 1) + " &ndash; " + (last) + "..."
                + "</span>");
        DOM.setStyleAttribute(scrollPositionElement, "display", "block");
    }

    private void hideScrollPositionAnnotation() {
        if (scrollPositionElement != null) {
            DOM.setStyleAttribute(scrollPositionElement, "display", "none");
        }
    }

    private class RowRequestHandler extends Timer {

        private int reqFirstRow = 0;
        private int reqRows = 0;

        public void deferRowFetch() {
            deferRowFetch(250);
        }

        public void deferRowFetch(int msec) {
            if (reqRows > 0 && reqFirstRow < totalRows) {
                schedule(msec);

                // tell scroll position to user if currently "visible" rows are
                // not rendered
                if ((firstRowInViewPort + pageLength > scrollBody
                        .getLastRendered())
                        || (firstRowInViewPort < scrollBody.getFirstRendered())) {
                    announceScrollPosition();
                } else {
                    hideScrollPositionAnnotation();
                }
            }
        }

        public void setReqFirstRow(int reqFirstRow) {
            if (reqFirstRow < 0) {
                reqFirstRow = 0;
            } else if (reqFirstRow >= totalRows) {
                reqFirstRow = totalRows - 1;
            }
            this.reqFirstRow = reqFirstRow;
        }

        public void setReqRows(int reqRows) {
            this.reqRows = reqRows;
        }

        @Override
        public void run() {
            if (client.hasActiveRequest()) {
                // if client connection is busy, don't bother loading it more
                schedule(250);

            } else {

                int firstToBeRendered = scrollBody.firstRendered;
                if (reqFirstRow < firstToBeRendered) {
                    firstToBeRendered = reqFirstRow;
                } else if (firstRowInViewPort - (int) (cache_rate * pageLength) > firstToBeRendered) {
                    firstToBeRendered = firstRowInViewPort
                            - (int) (cache_rate * pageLength);
                    if (firstToBeRendered < 0) {
                        firstToBeRendered = 0;
                    }
                }

                int lastToBeRendered = scrollBody.lastRendered;

                if (reqFirstRow + reqRows - 1 > lastToBeRendered) {
                    lastToBeRendered = reqFirstRow + reqRows - 1;
                } else if (firstRowInViewPort + pageLength + pageLength
                        * cache_rate < lastToBeRendered) {
                    lastToBeRendered = (firstRowInViewPort + pageLength + (int) (pageLength * cache_rate));
                    if (lastToBeRendered >= totalRows) {
                        lastToBeRendered = totalRows - 1;
                    }
                    // due Safari 3.1 bug (see #2607), verify reqrows, original
                    // problem unknown, but this should catch the issue
                    if (reqFirstRow + reqRows - 1 > lastToBeRendered) {
                        reqRows = lastToBeRendered - reqFirstRow;
                    }
                }

                client.updateVariable(paintableId, "firstToBeRendered",
                        firstToBeRendered, false);

                client.updateVariable(paintableId, "lastToBeRendered",
                        lastToBeRendered, false);
                // remember which firstvisible we requested, in case the server
                // has
                // a differing opinion
                lastRequestedFirstvisible = firstRowInViewPort;
                client.updateVariable(paintableId, "firstvisible",
                        firstRowInViewPort, false);
                client.updateVariable(paintableId, "reqfirstrow", reqFirstRow,
                        false);
                client.updateVariable(paintableId, "reqrows", reqRows, true);

            }
        }

        public int getReqFirstRow() {
            return reqFirstRow;
        }

        public int getReqRows() {
            return reqRows;
        }

        /**
         * Sends request to refresh content at this position.
         */
        public void refreshContent() {
            int first = (int) (firstRowInViewPort - pageLength * cache_rate);
            int reqRows = (int) (2 * pageLength * cache_rate + pageLength);
            if (first < 0) {
                reqRows = reqRows + first;
                first = 0;
            }
            setReqFirstRow(first);
            setReqRows(reqRows);
            run();
        }
    }

    public class HeaderCell extends Widget {

        Element td = DOM.createTD();

        Element captionContainer = DOM.createDiv();

        Element colResizeWidget = DOM.createDiv();

        Element floatingCopyOfHeaderCell;

        private boolean sortable = false;
        private final String cid;
        private boolean dragging;

        private int dragStartX;
        private int colIndex;
        private int originalWidth;

        private boolean isResizing;

        private int headerX;

        private boolean moved;

        private int closestSlot;

        private int width = -1;

        private int naturalWidth = -1;

        private char align = ALIGN_LEFT;

        boolean definedWidth = false;

        private float expandRatio = 0;

        public void setSortable(boolean b) {
            sortable = b;
        }

        public void setNaturalMinimumColumnWidth(int w) {
            naturalWidth = w;
        }

        public HeaderCell(String colId, String headerText) {
            cid = colId;

            DOM.setElementProperty(colResizeWidget, "className", CLASSNAME
                    + "-resizer");
            DOM.sinkEvents(colResizeWidget, Event.MOUSEEVENTS);

            setText(headerText);

            DOM.appendChild(td, colResizeWidget);

            DOM.setElementProperty(captionContainer, "className", CLASSNAME
                    + "-caption-container");

            // ensure no clipping initially (problem on column additions)
            DOM.setStyleAttribute(captionContainer, "overflow", "visible");

            DOM.sinkEvents(captionContainer, Event.MOUSEEVENTS);

            DOM.appendChild(td, captionContainer);

            DOM.sinkEvents(td, Event.MOUSEEVENTS);

            setElement(td);
        }

        public void setWidth(int w, boolean ensureDefinedWidth) {
            if (ensureDefinedWidth) {
                definedWidth = true;
                // on column resize expand ratio becomes zero
                expandRatio = 0;
            }
            if (width == w) {
                return;
            }
            if (width == -1) {
                // go to default mode, clip content if necessary
                DOM.setStyleAttribute(captionContainer, "overflow", "");
            }
            width = w;
            if (w == -1) {
                DOM.setStyleAttribute(captionContainer, "width", "");
                setWidth("");
            } else {

                captionContainer.getStyle().setPropertyPx("width", w);

                /*
                 * if we already have tBody, set the header width properly, if
                 * not defer it. IE will fail with complex float in table header
                 * unless TD width is not explicitly set.
                 */
                if (scrollBody != null) {
                    int tdWidth = width + scrollBody.getCellExtraWidth();
                    setWidth(tdWidth + "px");
                } else {
                    DeferredCommand.addCommand(new Command() {
                        public void execute() {
                            int tdWidth = width
                                    + scrollBody.getCellExtraWidth();
                            setWidth(tdWidth + "px");
                        }
                    });
                }
            }
        }

        public void setUndefinedWidth() {
            definedWidth = false;
            setWidth(-1, false);
        }

        /**
         * Detects if width is fixed by developer on server side or resized to
         * current width by user.
         * 
         * @return true if defined, false if "natural" width
         */
        public boolean isDefinedWidth() {
            return definedWidth;
        }

        public int getWidth() {
            return width;
        }

        public void setText(String headerText) {
            DOM.setInnerHTML(captionContainer, headerText);
        }

        public String getColKey() {
            return cid;
        }

        private void setSorted(boolean sorted) {
            if (sorted) {
                if (sortAscending) {
                    this.setStyleName(CLASSNAME + "-header-cell-asc");
                } else {
                    this.setStyleName(CLASSNAME + "-header-cell-desc");
                }
            } else {
                this.setStyleName(CLASSNAME + "-header-cell");
            }
        }

        /**
         * Handle column reordering.
         */
        @Override
        public void onBrowserEvent(Event event) {
            if (enabled && event != null) {
                if (isResizing || event.getTarget() == colResizeWidget) {
                    onResizeEvent(event);
                } else {
                    handleCaptionEvent(event);
                }
            }
        }

        private void createFloatingCopy() {
            floatingCopyOfHeaderCell = DOM.createDiv();
            DOM.setInnerHTML(floatingCopyOfHeaderCell, DOM.getInnerHTML(td));
            floatingCopyOfHeaderCell = DOM
                    .getChild(floatingCopyOfHeaderCell, 1);
            DOM.setElementProperty(floatingCopyOfHeaderCell, "className",
                    CLASSNAME + "-header-drag");
            updateFloatingCopysPosition(DOM.getAbsoluteLeft(td), DOM
                    .getAbsoluteTop(td));
            DOM.appendChild(RootPanel.get().getElement(),
                    floatingCopyOfHeaderCell);
        }

        private void updateFloatingCopysPosition(int x, int y) {
            x -= DOM.getElementPropertyInt(floatingCopyOfHeaderCell,
                    "offsetWidth") / 2;
            DOM.setStyleAttribute(floatingCopyOfHeaderCell, "left", x + "px");
            if (y > 0) {
                DOM.setStyleAttribute(floatingCopyOfHeaderCell, "top", (y + 7)
                        + "px");
            }
        }

        private void hideFloatingCopy() {
            DOM.removeChild(RootPanel.get().getElement(),
                    floatingCopyOfHeaderCell);
            floatingCopyOfHeaderCell = null;
        }

        protected void handleCaptionEvent(Event event) {
            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEDOWN:
                if (columnReordering) {
                    dragging = true;
                    moved = false;
                    colIndex = getColIndexByKey(cid);
                    DOM.setCapture(getElement());
                    headerX = tHead.getAbsoluteLeft();
                    DOM.eventPreventDefault(event); // prevent selecting text
                }
                break;
            case Event.ONMOUSEUP:
                if (columnReordering) {
                    dragging = false;
                    DOM.releaseCapture(getElement());
                    if (moved) {
                        hideFloatingCopy();
                        tHead.removeSlotFocus();
                        if (closestSlot != colIndex
                                && closestSlot != (colIndex + 1)) {
                            if (closestSlot > colIndex) {
                                reOrderColumn(cid, closestSlot - 1);
                            } else {
                                reOrderColumn(cid, closestSlot);
                            }
                        }
                    }
                }

                if (!moved) {
                    // mouse event was a click to header -> sort column
                    if (sortable) {
                        if (sortColumn.equals(cid)) {
                            // just toggle order
                            client.updateVariable(paintableId, "sortascending",
                                    !sortAscending, false);
                        } else {
                            // set table scrolled by this column
                            client.updateVariable(paintableId, "sortcolumn",
                                    cid, false);
                        }
                        // get also cache columns at the same request
                        bodyContainer.setScrollPosition(0);
                        firstvisible = 0;
                        rowRequestHandler.setReqFirstRow(0);
                        rowRequestHandler.setReqRows((int) (2 * pageLength
                                * cache_rate + pageLength));
                        rowRequestHandler.deferRowFetch();
                    }
                    break;
                }
                break;
            case Event.ONMOUSEMOVE:
                if (dragging) {
                    if (!moved) {
                        createFloatingCopy();
                        moved = true;
                    }
                    final int x = DOM.eventGetClientX(event)
                            + DOM.getElementPropertyInt(tHead.hTableWrapper,
                                    "scrollLeft");
                    int slotX = headerX;
                    closestSlot = colIndex;
                    int closestDistance = -1;
                    int start = 0;
                    if (showRowHeaders) {
                        start++;
                    }
                    final int visibleCellCount = tHead.getVisibleCellCount();
                    for (int i = start; i <= visibleCellCount; i++) {
                        if (i > 0) {
                            final String colKey = getColKeyByIndex(i - 1);
                            slotX += getColWidth(colKey);
                        }
                        final int dist = Math.abs(x - slotX);
                        if (closestDistance == -1 || dist < closestDistance) {
                            closestDistance = dist;
                            closestSlot = i;
                        }
                    }
                    tHead.focusSlot(closestSlot);

                    updateFloatingCopysPosition(DOM.eventGetClientX(event), -1);
                }
                break;
            default:
                break;
            }
        }

        private void onResizeEvent(Event event) {
            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEDOWN:
                isResizing = true;
                DOM.setCapture(getElement());
                dragStartX = DOM.eventGetClientX(event);
                colIndex = getColIndexByKey(cid);
                originalWidth = getWidth();
                DOM.eventPreventDefault(event);
                break;
            case Event.ONMOUSEUP:
                isResizing = false;
                DOM.releaseCapture(getElement());
                // readjust undefined width columns
                lazyAdjustColumnWidths.cancel();
                lazyAdjustColumnWidths.schedule(1);
                break;
            case Event.ONMOUSEMOVE:
                if (isResizing) {
                    final int deltaX = DOM.eventGetClientX(event) - dragStartX;
                    if (deltaX == 0) {
                        return;
                    }

                    int newWidth = originalWidth + deltaX;
                    if (newWidth < scrollBody.getCellExtraWidth()) {
                        newWidth = scrollBody.getCellExtraWidth();
                    }
                    setColWidth(colIndex, newWidth, true);
                }
                break;
            default:
                break;
            }
        }

        public String getCaption() {
            return DOM.getInnerText(captionContainer);
        }

        public boolean isEnabled() {
            return getParent() != null;
        }

        public void setAlign(char c) {
            if (align != c) {
                switch (c) {
                case ALIGN_CENTER:
                    DOM.setStyleAttribute(captionContainer, "textAlign",
                            "center");
                    break;
                case ALIGN_RIGHT:
                    DOM.setStyleAttribute(captionContainer, "textAlign",
                            "right");
                    break;
                default:
                    DOM.setStyleAttribute(captionContainer, "textAlign", "");
                    break;
                }
            }
            align = c;
        }

        public char getAlign() {
            return align;
        }

        /**
         * Detects the natural minimum width for the column of this header cell.
         * If column is resized by user or the width is defined by server the
         * actual width is returned. Else the natural min width is returned.
         * 
         * @param columnIndex
         *            column index hint, if -1 (unknown) it will be detected
         * 
         * @return
         */
        public int getNaturalColumnWidth(int columnIndex) {
            if (isDefinedWidth()) {
                return width;
            } else {
                if (naturalWidth < 0) {
                    // This is recently revealed column. Try to detect a proper
                    // value (greater of header and data
                    // cols)

                    final int hw = ((Element) getElement().getLastChild())
                            .getOffsetWidth()
                            + scrollBody.getCellExtraWidth();
                    if (columnIndex < 0) {
                        columnIndex = 0;
                        for (Iterator<Widget> it = tHead.iterator(); it
                                .hasNext(); columnIndex++) {
                            if (it.next() == this) {
                                break;
                            }
                        }
                    }
                    final int cw = scrollBody.getColWidth(columnIndex);
                    naturalWidth = (hw > cw ? hw : cw);
                }
                return naturalWidth;
            }
        }

        public void setExpandRatio(float floatAttribute) {
            expandRatio = floatAttribute;
        }

        public float getExpandRatio() {
            return expandRatio;
        }

    }

    /**
     * HeaderCell that is header cell for row headers.
     * 
     * Reordering disabled and clicking on it resets sorting.
     */
    public class RowHeadersHeaderCell extends HeaderCell {

        RowHeadersHeaderCell() {
            super("0", "");
        }

        @Override
        protected void handleCaptionEvent(Event event) {
            // NOP: RowHeaders cannot be reordered
            // TODO It'd be nice to reset sorting here
        }
    }

    public class TableHead extends Panel implements ActionOwner {

        private static final int WRAPPER_WIDTH = 9000;

        ArrayList<Widget> visibleCells = new ArrayList<Widget>();

        HashMap<String, HeaderCell> availableCells = new HashMap<String, HeaderCell>();

        Element div = DOM.createDiv();
        Element hTableWrapper = DOM.createDiv();
        Element hTableContainer = DOM.createDiv();
        Element table = DOM.createTable();
        Element headerTableBody = DOM.createTBody();
        Element tr = DOM.createTR();

        private final Element columnSelector = DOM.createDiv();

        private int focusedSlot = -1;

        public TableHead() {
            if (BrowserInfo.get().isIE()) {
                table.setPropertyInt("cellSpacing", 0);
            }

            DOM.setStyleAttribute(hTableWrapper, "overflow", "hidden");
            DOM.setElementProperty(hTableWrapper, "className", CLASSNAME
                    + "-header");

            // TODO move styles to CSS
            DOM.setElementProperty(columnSelector, "className", CLASSNAME
                    + "-column-selector");
            DOM.setStyleAttribute(columnSelector, "display", "none");

            DOM.appendChild(table, headerTableBody);
            DOM.appendChild(headerTableBody, tr);
            DOM.appendChild(hTableContainer, table);
            DOM.appendChild(hTableWrapper, hTableContainer);
            DOM.appendChild(div, hTableWrapper);
            DOM.appendChild(div, columnSelector);
            setElement(div);

            setStyleName(CLASSNAME + "-header-wrap");

            DOM.sinkEvents(columnSelector, Event.ONCLICK);

            availableCells.put("0", new RowHeadersHeaderCell());
        }

        @Override
        public void clear() {
            for (String cid : availableCells.keySet()) {
                removeCell(cid);
            }
            availableCells.clear();
            availableCells.put("0", new RowHeadersHeaderCell());
        }

        public void updateCellsFromUIDL(UIDL uidl) {
            Iterator<?> it = uidl.getChildIterator();
            HashSet<String> updated = new HashSet<String>();
            updated.add("0");
            while (it.hasNext()) {
                final UIDL col = (UIDL) it.next();
                final String cid = col.getStringAttribute("cid");
                updated.add(cid);

                String caption = buildCaptionHtmlSnippet(col);
                HeaderCell c = getHeaderCell(cid);
                if (c == null) {
                    c = new HeaderCell(cid, caption);
                    availableCells.put(cid, c);
                    if (initializedAndAttached) {
                        // we will need a column width recalculation
                        initializedAndAttached = false;
                        initialContentReceived = false;
                        isNewBody = true;
                    }
                } else {
                    c.setText(caption);
                }

                if (col.hasAttribute("sortable")) {
                    c.setSortable(true);
                    if (cid.equals(sortColumn)) {
                        c.setSorted(true);
                    } else {
                        c.setSorted(false);
                    }
                } else {
                    c.setSortable(false);
                }

                if (col.hasAttribute("align")) {
                    c.setAlign(col.getStringAttribute("align").charAt(0));
                }
                if (col.hasAttribute("width")) {
                    final String width = col.getStringAttribute("width");
                    c.setWidth(Integer.parseInt(width), true);
                } else if (recalcWidths) {
                    c.setUndefinedWidth();
                }
                if (col.hasAttribute("er")) {
                    c.setExpandRatio(col.getFloatAttribute("er"));
                }
                if (col.hasAttribute("collapsed")) {
                    // ensure header is properly removed from parent (case when
                    // collapsing happens via servers side api)
                    if (c.isAttached()) {
                        c.removeFromParent();
                        headerChangedDuringUpdate = true;
                    }
                }

            }
            // check for orphaned header cells
            for (Iterator<String> cit = availableCells.keySet().iterator(); cit
                    .hasNext();) {
                String cid = cit.next();
                if (!updated.contains(cid)) {
                    removeCell(cid);
                    cit.remove();
                }
            }

        }

        public void enableColumn(String cid, int index) {
            final HeaderCell c = getHeaderCell(cid);
            if (!c.isEnabled() || getHeaderCell(index) != c) {
                setHeaderCell(index, c);
                if (initializedAndAttached) {
                    headerChangedDuringUpdate = true;
                }
            }
        }

        public int getVisibleCellCount() {
            return visibleCells.size();
        }

        public void setHorizontalScrollPosition(int scrollLeft) {
            if (BrowserInfo.get().isIE6()) {
                hTableWrapper.getStyle().setProperty("position", "relative");
                hTableWrapper.getStyle().setPropertyPx("left", -scrollLeft);
            } else {
                hTableWrapper.setScrollLeft(scrollLeft);
            }
        }

        public void setColumnCollapsingAllowed(boolean cc) {
            if (cc) {
                DOM.setStyleAttribute(columnSelector, "display", "block");
            } else {
                DOM.setStyleAttribute(columnSelector, "display", "none");
            }
        }

        public void disableBrowserIntelligence() {
            DOM.setStyleAttribute(hTableContainer, "width", WRAPPER_WIDTH
                    + "px");
        }

        public void enableBrowserIntelligence() {
            DOM.setStyleAttribute(hTableContainer, "width", "");
        }

        public void setHeaderCell(int index, HeaderCell cell) {
            if (cell.isEnabled()) {
                // we're moving the cell
                DOM.removeChild(tr, cell.getElement());
                orphan(cell);
            }
            if (index < visibleCells.size()) {
                // insert to right slot
                DOM.insertChild(tr, cell.getElement(), index);
                adopt(cell);
                visibleCells.add(index, cell);
            } else if (index == visibleCells.size()) {
                // simply append
                DOM.appendChild(tr, cell.getElement());
                adopt(cell);
                visibleCells.add(cell);
            } else {
                throw new RuntimeException(
                        "Header cells must be appended in order");
            }
        }

        public HeaderCell getHeaderCell(int index) {
            if (index < visibleCells.size()) {
                return (HeaderCell) visibleCells.get(index);
            } else {
                return null;
            }
        }

        /**
         * Get's HeaderCell by it's column Key.
         * 
         * Note that this returns HeaderCell even if it is currently collapsed.
         * 
         * @param cid
         *            Column key of accessed HeaderCell
         * @return HeaderCell
         */
        public HeaderCell getHeaderCell(String cid) {
            return availableCells.get(cid);
        }

        public void moveCell(int oldIndex, int newIndex) {
            final HeaderCell hCell = getHeaderCell(oldIndex);
            final Element cell = hCell.getElement();

            visibleCells.remove(oldIndex);
            DOM.removeChild(tr, cell);

            DOM.insertChild(tr, cell, newIndex);
            visibleCells.add(newIndex, hCell);
        }

        public Iterator<Widget> iterator() {
            return visibleCells.iterator();
        }

        @Override
        public boolean remove(Widget w) {
            if (visibleCells.contains(w)) {
                visibleCells.remove(w);
                orphan(w);
                DOM.removeChild(DOM.getParent(w.getElement()), w.getElement());
                return true;
            }
            return false;
        }

        public void removeCell(String colKey) {
            final HeaderCell c = getHeaderCell(colKey);
            remove(c);
        }

        private void focusSlot(int index) {
            removeSlotFocus();
            if (index > 0) {
                DOM.setElementProperty(DOM.getFirstChild(DOM.getChild(tr,
                        index - 1)), "className", CLASSNAME + "-resizer "
                        + CLASSNAME + "-focus-slot-right");
            } else {
                DOM.setElementProperty(DOM.getFirstChild(DOM
                        .getChild(tr, index)), "className", CLASSNAME
                        + "-resizer " + CLASSNAME + "-focus-slot-left");
            }
            focusedSlot = index;
        }

        private void removeSlotFocus() {
            if (focusedSlot < 0) {
                return;
            }
            if (focusedSlot == 0) {
                DOM.setElementProperty(DOM.getFirstChild(DOM.getChild(tr,
                        focusedSlot)), "className", CLASSNAME + "-resizer");
            } else if (focusedSlot > 0) {
                DOM.setElementProperty(DOM.getFirstChild(DOM.getChild(tr,
                        focusedSlot - 1)), "className", CLASSNAME + "-resizer");
            }
            focusedSlot = -1;
        }

        @Override
        public void onBrowserEvent(Event event) {
            if (enabled) {
                if (event.getTarget() == columnSelector) {
                    final int left = DOM.getAbsoluteLeft(columnSelector);
                    final int top = DOM.getAbsoluteTop(columnSelector)
                            + DOM.getElementPropertyInt(columnSelector,
                                    "offsetHeight");
                    client.getContextMenu().showAt(this, left, top);
                }
            }
        }

        class VisibleColumnAction extends Action {

            String colKey;
            private boolean collapsed;

            public VisibleColumnAction(String colKey) {
                super(VScrollTable.TableHead.this);
                this.colKey = colKey;
                caption = tHead.getHeaderCell(colKey).getCaption();
            }

            @Override
            public void execute() {
                client.getContextMenu().hide();
                // toggle selected column
                if (collapsedColumns.contains(colKey)) {
                    collapsedColumns.remove(colKey);
                } else {
                    tHead.removeCell(colKey);
                    collapsedColumns.add(colKey);
                    lazyAdjustColumnWidths.schedule(1);
                }

                // update variable to server
                client.updateVariable(paintableId, "collapsedcolumns",
                        collapsedColumns.toArray(new String[collapsedColumns
                                .size()]), false);
                // let rowRequestHandler determine proper rows
                rowRequestHandler.refreshContent();
            }

            public void setCollapsed(boolean b) {
                collapsed = b;
            }

            /**
             * Override default method to distinguish on/off columns
             */
            @Override
            public String getHTML() {
                final StringBuffer buf = new StringBuffer();
                if (collapsed) {
                    buf.append("<span class=\"v-off\">");
                } else {
                    buf.append("<span class=\"v-on\">");
                }
                buf.append(super.getHTML());
                buf.append("</span>");

                return buf.toString();
            }

        }

        /*
         * Returns columns as Action array for column select popup
         */
        public Action[] getActions() {
            Object[] cols;
            if (columnReordering) {
                cols = columnOrder;
            } else {
                // if columnReordering is disabled, we need different way to get
                // all available columns
                cols = visibleColOrder;
                cols = new Object[visibleColOrder.length
                        + collapsedColumns.size()];
                int i;
                for (i = 0; i < visibleColOrder.length; i++) {
                    cols[i] = visibleColOrder[i];
                }
                for (final Iterator<String> it = collapsedColumns.iterator(); it
                        .hasNext();) {
                    cols[i++] = it.next();
                }
            }
            final Action[] actions = new Action[cols.length];

            for (int i = 0; i < cols.length; i++) {
                final String cid = (String) cols[i];
                final HeaderCell c = getHeaderCell(cid);
                final VisibleColumnAction a = new VisibleColumnAction(c
                        .getColKey());
                a.setCaption(c.getCaption());
                if (!c.isEnabled()) {
                    a.setCollapsed(true);
                }
                actions[i] = a;
            }
            return actions;
        }

        public ApplicationConnection getClient() {
            return client;
        }

        public String getPaintableId() {
            return paintableId;
        }

        /**
         * Returns column alignments for visible columns
         */
        public char[] getColumnAlignments() {
            final Iterator<Widget> it = visibleCells.iterator();
            final char[] aligns = new char[visibleCells.size()];
            int colIndex = 0;
            while (it.hasNext()) {
                aligns[colIndex++] = ((HeaderCell) it.next()).getAlign();
            }
            return aligns;
        }

    }

    /**
     * This Panel can only contain VScrollTableRow type of widgets. This
     * "simulates" very large table, keeping spacers which take room of
     * unrendered rows.
     * 
     */
    public class VScrollTableBody extends Panel {

        public static final int DEFAULT_ROW_HEIGHT = 24;

        private double rowHeight = -1;

        private final List<Widget> renderedRows = new ArrayList<Widget>();

        /**
         * Due some optimizations row height measuring is deferred and initial
         * set of rows is rendered detached. Flag set on when table body has
         * been attached in dom and rowheight has been measured.
         */
        private boolean tBodyMeasurementsDone = false;

        Element preSpacer = DOM.createDiv();
        Element postSpacer = DOM.createDiv();

        Element container = DOM.createDiv();

        TableSectionElement tBodyElement = Document.get().createTBodyElement();
        Element table = DOM.createTable();

        private int firstRendered;

        private int lastRendered;

        private char[] aligns;

        VScrollTableBody() {
            constructDOM();

            setElement(container);
        }

        /**
         * @return the height of scrollable body, subpixels ceiled.
         */
        public int getRequiredHeight() {
            return preSpacer.getOffsetHeight() + postSpacer.getOffsetHeight()
                    + Util.getRequiredHeight(table);
        }

        private void constructDOM() {
            DOM.setElementProperty(table, "className", CLASSNAME + "-table");
            if (BrowserInfo.get().isIE()) {
                table.setPropertyInt("cellSpacing", 0);
            }
            DOM.setElementProperty(preSpacer, "className", CLASSNAME
                    + "-row-spacer");
            DOM.setElementProperty(postSpacer, "className", CLASSNAME
                    + "-row-spacer");

            table.appendChild(tBodyElement);
            DOM.appendChild(container, preSpacer);
            DOM.appendChild(container, table);
            DOM.appendChild(container, postSpacer);

        }

        public int getAvailableWidth() {
            int availW = bodyContainer.getOffsetWidth() - getBorderWidth();
            return availW;
        }

        public void renderInitialRows(UIDL rowData, int firstIndex, int rows) {
            firstRendered = firstIndex;
            lastRendered = firstIndex + rows - 1;
            final Iterator<?> it = rowData.getChildIterator();
            aligns = tHead.getColumnAlignments();
            while (it.hasNext()) {
                final VScrollTableRow row = new VScrollTableRow((UIDL) it
                        .next(), aligns);
                addRow(row);
            }
            if (isAttached()) {
                fixSpacers();
            }
        }

        public void renderRows(UIDL rowData, int firstIndex, int rows) {
            // FIXME REVIEW
            aligns = tHead.getColumnAlignments();
            final Iterator<?> it = rowData.getChildIterator();
            if (firstIndex == lastRendered + 1) {
                while (it.hasNext()) {
                    final VScrollTableRow row = createRow((UIDL) it.next());
                    addRow(row);
                    lastRendered++;
                }
                fixSpacers();
            } else if (firstIndex + rows == firstRendered) {
                final VScrollTableRow[] rowArray = new VScrollTableRow[rows];
                int i = rows;
                while (it.hasNext()) {
                    i--;
                    rowArray[i] = createRow((UIDL) it.next());
                }
                for (i = 0; i < rows; i++) {
                    addRowBeforeFirstRendered(rowArray[i]);
                    firstRendered--;
                }
            } else {
                // completely new set of rows
                while (lastRendered + 1 > firstRendered) {
                    unlinkRow(false);
                }
                final VScrollTableRow row = createRow((UIDL) it.next());
                firstRendered = firstIndex;
                lastRendered = firstIndex - 1;
                addRow(row);
                lastRendered++;
                setContainerHeight();
                fixSpacers();
                while (it.hasNext()) {
                    addRow(createRow((UIDL) it.next()));
                    lastRendered++;
                }
                fixSpacers();
            }
            // this may be a new set of rows due content change,
            // ensure we have proper cache rows
            int reactFirstRow = (int) (firstRowInViewPort - pageLength
                    * cache_react_rate);
            int reactLastRow = (int) (firstRowInViewPort + pageLength + pageLength
                    * cache_react_rate);
            if (reactFirstRow < 0) {
                reactFirstRow = 0;
            }
            if (reactLastRow >= totalRows) {
                reactLastRow = totalRows - 1;
            }
            if (lastRendered < reactLastRow) {
                ApplicationConnection.getConsole().log("GET BELOW");
                // get some cache rows below visible area
                rowRequestHandler.setReqFirstRow(lastRendered + 1);
                rowRequestHandler.setReqRows(reactLastRow - lastRendered);
                rowRequestHandler.deferRowFetch(1);
            } else if (scrollBody.getFirstRendered() > reactFirstRow) {
                ApplicationConnection.getConsole().log("GET ABOVE");
                /*
                 * Branch for fetching cache above visible area.
                 * 
                 * If cache needed for both before and after visible area, this
                 * will be rendered after-cache is reveived and rendered. So in
                 * some rare situations table may take two cache visits to
                 * server.
                 */
                rowRequestHandler.setReqFirstRow(reactFirstRow);
                rowRequestHandler.setReqRows(firstRendered - reactFirstRow);
                rowRequestHandler.deferRowFetch(1);
            }
        }

        /**
         * This method is used to instantiate new rows for this table. It
         * automatically sets correct widths to rows cells and assigns correct
         * client reference for child widgets.
         * 
         * This method can be called only after table has been initialized
         * 
         * @param uidl
         */
        private VScrollTableRow createRow(UIDL uidl) {
            final VScrollTableRow row = new VScrollTableRow(uidl, aligns);
            final int cells = DOM.getChildCount(row.getElement());
            for (int i = 0; i < cells; i++) {
                final Element cell = DOM.getChild(row.getElement(), i);
                int w = VScrollTable.this.getColWidth(getColKeyByIndex(i));
                if (w < 0) {
                    w = 0;
                }
                cell.getFirstChildElement().getStyle()
                        .setPropertyPx("width", w);
                cell.getStyle().setPropertyPx("width", w);
            }
            return row;
        }

        private void addRowBeforeFirstRendered(VScrollTableRow row) {
            VScrollTableRow first = null;
            if (renderedRows.size() > 0) {
                first = (VScrollTableRow) renderedRows.get(0);
            }
            if (first != null && first.getStyleName().indexOf("-odd") == -1) {
                row.addStyleName(CLASSNAME + "-row-odd");
            } else {
                row.addStyleName(CLASSNAME + "-row");
            }
            if (row.isSelected()) {
                row.addStyleName("v-selected");
            }
            tBodyElement.insertBefore(row.getElement(), tBodyElement
                    .getFirstChild());
            adopt(row);
            renderedRows.add(0, row);
        }

        private void addRow(VScrollTableRow row) {
            VScrollTableRow last = null;
            if (renderedRows.size() > 0) {
                last = (VScrollTableRow) renderedRows
                        .get(renderedRows.size() - 1);
            }
            if (last != null && last.getStyleName().indexOf("-odd") == -1) {
                row.addStyleName(CLASSNAME + "-row-odd");
            } else {
                row.addStyleName(CLASSNAME + "-row");
            }
            if (row.isSelected()) {
                row.addStyleName("v-selected");
            }
            tBodyElement.appendChild(row.getElement());
            adopt(row);
            renderedRows.add(row);
        }

        public Iterator<Widget> iterator() {
            return renderedRows.iterator();
        }

        /**
         * @return false if couldn't remove row
         */
        public boolean unlinkRow(boolean fromBeginning) {
            if (lastRendered - firstRendered < 0) {
                return false;
            }
            int index;
            if (fromBeginning) {
                index = 0;
                firstRendered++;
            } else {
                index = renderedRows.size() - 1;
                lastRendered--;
            }
            if (index >= 0) {
                final VScrollTableRow toBeRemoved = (VScrollTableRow) renderedRows
                        .get(index);
                lazyUnregistryBag.add(toBeRemoved);
                tBodyElement.removeChild(toBeRemoved.getElement());
                orphan(toBeRemoved);
                renderedRows.remove(index);
                fixSpacers();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean remove(Widget w) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void onAttach() {
            super.onAttach();
            setContainerHeight();
        }

        /**
         * Fix container blocks height according to totalRows to avoid
         * "bouncing" when scrolling
         */
        private void setContainerHeight() {
            fixSpacers();
            DOM.setStyleAttribute(container, "height", totalRows
                    * getRowHeight() + "px");
        }

        private void fixSpacers() {
            int prepx = (int) Math.round(getRowHeight() * firstRendered);
            if (prepx < 0) {
                prepx = 0;
            }
            preSpacer.getStyle().setPropertyPx("height", prepx);
            int postpx = (int) (getRowHeight() * (totalRows - 1 - lastRendered));
            if (postpx < 0) {
                postpx = 0;
            }
            postSpacer.getStyle().setPropertyPx("height", postpx);
        }

        public double getRowHeight() {
            return getRowHeight(false);
        }

        public double getRowHeight(boolean forceUpdate) {
            if (tBodyMeasurementsDone && !forceUpdate) {
                return rowHeight;
            } else {

                if (tBodyElement.getRows().getLength() > 0) {
                    int tableHeight = getTableHeight();
                    int rowCount = tBodyElement.getRows().getLength();
                    rowHeight = tableHeight / (double) rowCount;
                } else {
                    if (isAttached()) {
                        // measure row height by adding a dummy row
                        VScrollTableRow scrollTableRow = new VScrollTableRow();
                        tBodyElement.appendChild(scrollTableRow.getElement());
                        getRowHeight(forceUpdate);
                        tBodyElement.removeChild(scrollTableRow.getElement());
                    } else {
                        // TODO investigate if this can never happen anymore
                        return DEFAULT_ROW_HEIGHT;
                    }
                }
                tBodyMeasurementsDone = true;
                return rowHeight;
            }
        }

        public int getTableHeight() {
            return table.getOffsetHeight();
        }

        /**
         * Returns the width available for column content.
         * 
         * @param columnIndex
         * @return
         */
        public int getColWidth(int columnIndex) {
            if (tBodyMeasurementsDone) {
                NodeList<TableRowElement> rows = tBodyElement.getRows();
                if (rows.getLength() == 0) {
                    // no rows yet rendered
                    return 0;
                } else {
                    com.google.gwt.dom.client.Element wrapperdiv = rows
                            .getItem(0).getCells().getItem(columnIndex)
                            .getFirstChildElement();
                    return wrapperdiv.getOffsetWidth();
                }
            } else {
                return 0;
            }
        }

        /**
         * Sets the content width of a column.
         * 
         * Due IE limitation, we must set the width to a wrapper elements inside
         * table cells (with overflow hidden, which does not work on td
         * elements).
         * 
         * To get this work properly crossplatform, we will also set the width
         * of td.
         * 
         * @param colIndex
         * @param w
         */
        public void setColWidth(int colIndex, int w) {
            NodeList<TableRowElement> rows2 = tBodyElement.getRows();
            final int rows = rows2.getLength();
            for (int i = 0; i < rows; i++) {
                TableRowElement row = rows2.getItem(i);
                TableCellElement cell = row.getCells().getItem(colIndex);
                cell.getFirstChildElement().getStyle()
                        .setPropertyPx("width", w);
                cell.getStyle().setPropertyPx("width", w);
            }
        }

        private int cellExtraWidth = -1;

        /**
         * Method to return the space used for cell paddings + border.
         */
        private int getCellExtraWidth() {
            if (cellExtraWidth < 0) {
                detectExtrawidth();
            }
            return cellExtraWidth;
        }

        private void detectExtrawidth() {
            NodeList<TableRowElement> rows = tBodyElement.getRows();
            if (rows.getLength() == 0) {
                /* need to temporary add empty row and detect */
                VScrollTableRow scrollTableRow = new VScrollTableRow();
                tBodyElement.appendChild(scrollTableRow.getElement());
                detectExtrawidth();
                tBodyElement.removeChild(scrollTableRow.getElement());
            } else {
                boolean noCells = false;
                TableRowElement item = rows.getItem(0);
                TableCellElement firstTD = item.getCells().getItem(0);
                if (firstTD == null) {
                    // content is currently empty, we need to add a fake cell
                    // for measuring
                    noCells = true;
                    VScrollTableRow next = (VScrollTableRow) iterator().next();
                    next.addCell("", ALIGN_LEFT, "", true);
                    firstTD = item.getCells().getItem(0);
                }
                com.google.gwt.dom.client.Element wrapper = firstTD
                        .getFirstChildElement();
                cellExtraWidth = firstTD.getOffsetWidth()
                        - wrapper.getOffsetWidth();
                if (noCells) {
                    firstTD.getParentElement().removeChild(firstTD);
                }
            }
        }

        private void reLayoutComponents() {
            for (Widget w : this) {
                VScrollTableRow r = (VScrollTableRow) w;
                for (Widget widget : r) {
                    client.handleComponentRelativeSize(widget);
                }
            }
        }

        public int getLastRendered() {
            return lastRendered;
        }

        public int getFirstRendered() {
            return firstRendered;
        }

        public void moveCol(int oldIndex, int newIndex) {

            // loop all rows and move given index to its new place
            final Iterator<?> rows = iterator();
            while (rows.hasNext()) {
                final VScrollTableRow row = (VScrollTableRow) rows.next();

                final Element td = DOM.getChild(row.getElement(), oldIndex);
                DOM.removeChild(row.getElement(), td);

                DOM.insertChild(row.getElement(), td, newIndex);

            }

        }

        /**
         * Restore row visibility which is set to "none" when the row is
         * rendered (due a performance optimization).
         */
        private void restoreRowVisibility() {
            for (Widget row : renderedRows) {
                row.getElement().getStyle().setProperty("visibility", "");
            }
        }

        public class VScrollTableRow extends Panel implements ActionOwner,
                Container {

            ArrayList<Widget> childWidgets = new ArrayList<Widget>();
            private boolean selected = false;
            private final int rowKey;
            private List<UIDL> pendingComponentPaints;

            private String[] actionKeys = null;
            private final TableRowElement rowElement;

            private VScrollTableRow(int rowKey) {
                this.rowKey = rowKey;
                rowElement = Document.get().createTRElement();
                setElement(rowElement);
                DOM.sinkEvents(getElement(), Event.ONMOUSEUP | Event.ONDBLCLICK
                        | Event.ONCONTEXTMENU);
            }

            private void paintComponent(Paintable p, UIDL uidl) {
                if (isAttached()) {
                    p.updateFromUIDL(uidl, client);
                } else {
                    if (pendingComponentPaints == null) {
                        pendingComponentPaints = new LinkedList<UIDL>();
                    }
                    pendingComponentPaints.add(uidl);
                }
            }

            @Override
            protected void onAttach() {
                super.onAttach();
                if (pendingComponentPaints != null) {
                    for (UIDL uidl : pendingComponentPaints) {
                        Paintable paintable = client.getPaintable(uidl);
                        paintable.updateFromUIDL(uidl, client);
                    }
                }
            }

            public String getKey() {
                return String.valueOf(rowKey);
            }

            public VScrollTableRow(UIDL uidl, char[] aligns) {
                this(uidl.getIntAttribute("key"));

                /*
                 * Rendering the rows as hidden improves Firefox and Safari
                 * performance drastically.
                 */
                getElement().getStyle().setProperty("visibility", "hidden");

                String rowStyle = uidl.getStringAttribute("rowstyle");
                if (rowStyle != null) {
                    addStyleName(CLASSNAME + "-row-" + rowStyle);
                }

                tHead.getColumnAlignments();
                int col = 0;
                int visibleColumnIndex = -1;

                // row header
                if (showRowHeaders) {
                    addCell(buildCaptionHtmlSnippet(uidl), aligns[col++], "",
                            true);
                }

                if (uidl.hasAttribute("al")) {
                    actionKeys = uidl.getStringArrayAttribute("al");
                }

                final Iterator<?> cells = uidl.getChildIterator();
                while (cells.hasNext()) {
                    final Object cell = cells.next();
                    visibleColumnIndex++;

                    String columnId = visibleColOrder[visibleColumnIndex];

                    String style = "";
                    if (uidl.hasAttribute("style-" + columnId)) {
                        style = uidl.getStringAttribute("style-" + columnId);
                    }

                    if (cell instanceof String) {
                        addCell(cell.toString(), aligns[col++], style, false);
                    } else {
                        final Paintable cellContent = client
                                .getPaintable((UIDL) cell);

                        addCell((Widget) cellContent, aligns[col++], style);
                        paintComponent(cellContent, (UIDL) cell);
                    }
                }
                if (uidl.hasAttribute("selected") && !isSelected()) {
                    toggleSelection();
                }
            }

            /**
             * Add a dummy row, used for measurements if Table is empty.
             */
            public VScrollTableRow() {
                this(0);
                addStyleName(CLASSNAME + "-row");
                addCell("_", 'b', "", true);
            }

            public void addCell(String text, char align, String style,
                    boolean textIsHTML) {
                // String only content is optimized by not using Label widget
                final Element td = DOM.createTD();
                final Element container = DOM.createDiv();
                String className = CLASSNAME + "-cell-content";
                if (style != null && !style.equals("")) {
                    className += " " + CLASSNAME + "-cell-content-" + style;
                }
                td.setClassName(className);
                container.setClassName(CLASSNAME + "-cell-wrapper");
                if (textIsHTML) {
                    container.setInnerHTML(text);
                } else {
                    container.setInnerText(text);
                }
                if (align != ALIGN_LEFT) {
                    switch (align) {
                    case ALIGN_CENTER:
                        container.getStyle().setProperty("textAlign", "center");
                        break;
                    case ALIGN_RIGHT:
                    default:
                        container.getStyle().setProperty("textAlign", "right");
                        break;
                    }
                }
                td.appendChild(container);
                getElement().appendChild(td);
            }

            public void addCell(Widget w, char align, String style) {
                final Element td = DOM.createTD();
                final Element container = DOM.createDiv();
                String className = CLASSNAME + "-cell-content";
                if (style != null && !style.equals("")) {
                    className += " " + CLASSNAME + "-cell-content-" + style;
                }
                td.setClassName(className);
                container.setClassName(CLASSNAME + "-cell-wrapper");
                // TODO most components work with this, but not all (e.g.
                // Select)
                // Old comment: make widget cells respect align.
                // text-align:center for IE, margin: auto for others
                if (align != ALIGN_LEFT) {
                    switch (align) {
                    case ALIGN_CENTER:
                        container.getStyle().setProperty("textAlign", "center");
                        break;
                    case ALIGN_RIGHT:
                    default:
                        container.getStyle().setProperty("textAlign", "right");
                        break;
                    }
                }
                td.appendChild(container);
                getElement().appendChild(td);
                // ensure widget not attached to another element (possible tBody
                // change)
                w.removeFromParent();
                container.appendChild(w.getElement());
                adopt(w);
                childWidgets.add(w);
            }

            public Iterator<Widget> iterator() {
                return childWidgets.iterator();
            }

            @Override
            public boolean remove(Widget w) {
                if (childWidgets.contains(w)) {
                    orphan(w);
                    DOM.removeChild(DOM.getParent(w.getElement()), w
                            .getElement());
                    childWidgets.remove(w);
                    return true;
                } else {
                    return false;
                }
            }

            private void handleClickEvent(Event event, Element targetTdOrTr) {
                if (client.hasEventListeners(VScrollTable.this,
                        ITEM_CLICK_EVENT_ID)) {
                    boolean doubleClick = (DOM.eventGetType(event) == Event.ONDBLCLICK);

                    /* This row was clicked */
                    client.updateVariable(paintableId, "clickedKey", ""
                            + rowKey, false);

                    if (getElement() == targetTdOrTr.getParentElement()) {
                        /* A specific column was clicked */
                        int childIndex = DOM.getChildIndex(getElement(),
                                targetTdOrTr);
                        String colKey = null;
                        colKey = tHead.getHeaderCell(childIndex).getColKey();
                        client.updateVariable(paintableId, "clickedColKey",
                                colKey, false);
                    }

                    MouseEventDetails details = new MouseEventDetails(event);
                    // Note: the 'immediate' logic would need to be more
                    // involved (see #2104), but iscrolltable always sends
                    // select event, even though nullselectionallowed wont let
                    // the change trough. Will need to be updated if that is
                    // changed.
                    client
                            .updateVariable(
                                    paintableId,
                                    "clickEvent",
                                    details.toString(),
                                    !(event.getButton() == Event.BUTTON_LEFT
                                            && !doubleClick
                                            && selectMode > Table.SELECT_MODE_NONE && immediate));
                }
            }

            /*
             * React on click that occur on content cells only
             */
            @Override
            public void onBrowserEvent(Event event) {
                if (enabled) {
                    Element targetTdOrTr = getEventTargetTdOrTr(event);
                    if (targetTdOrTr != null) {
                        switch (DOM.eventGetType(event)) {
                        case Event.ONDBLCLICK:
                            handleClickEvent(event, targetTdOrTr);
                            break;
                        case Event.ONMOUSEUP:
                            handleClickEvent(event, targetTdOrTr);
                            if (event.getButton() == Event.BUTTON_LEFT
                                    && selectMode > Table.SELECT_MODE_NONE) {
                                toggleSelection();
                                // Note: changing the immediateness of this
                                // might
                                // require changes to "clickEvent" immediateness
                                // also.
                                client
                                        .updateVariable(
                                                paintableId,
                                                "selected",
                                                selectedRowKeys
                                                        .toArray(new String[selectedRowKeys
                                                                .size()]),
                                                immediate);
                            }
                            break;
                        case Event.ONCONTEXTMENU:
                            showContextMenu(event);
                            break;
                        default:
                            break;
                        }
                    }
                }
                super.onBrowserEvent(event);
            }

            /**
             * Finds the TD that the event interacts with. Returns null if the
             * target of the event should not be handled. If the event target is
             * the row directly this method returns the TR element instead of
             * the TD.
             * 
             * @param event
             * @return TD or TR element that the event targets (the actual event
             *         target is this element or a child of it)
             */
            private Element getEventTargetTdOrTr(Event event) {
                Element targetTdOrTr = null;

                final Element eventTarget = DOM.eventGetTarget(event);
                final Element eventTargetParent = DOM.getParent(eventTarget);
                final Element eventTargetGrandParent = DOM
                        .getParent(eventTargetParent);

                final Element thisTrElement = getElement();

                if (eventTarget == thisTrElement) {
                    // This was a click on the TR element
                    targetTdOrTr = eventTarget;
                    // rowTarget = true;
                } else if (thisTrElement == eventTargetParent) {
                    // Target parent is the TR, so the actual target is the TD
                    targetTdOrTr = eventTarget;
                } else if (thisTrElement == eventTargetGrandParent) {
                    // Target grand parent is the TR, so the parent is the TD
                    targetTdOrTr = eventTargetParent;
                } else {
                    /*
                     * This is a workaround to make Labels and Embedded in a
                     * Table clickable (see #2688). It is really not a fix as it
                     * does not work for a custom component (not extending
                     * VLabel/VEmbedded) or for read only textfields etc.
                     */
                    Element tdElement = eventTargetParent;
                    while (DOM.getParent(tdElement) != thisTrElement) {
                        tdElement = DOM.getParent(tdElement);
                    }

                    Element componentElement = tdElement.getFirstChildElement()
                            .getFirstChildElement().cast();
                    Widget widget = (Widget) client
                            .getPaintable(componentElement);
                    if (widget instanceof VLabel || widget instanceof VEmbedded) {
                        targetTdOrTr = tdElement;
                    }
                }

                return targetTdOrTr;
            }

            public void showContextMenu(Event event) {
                if (enabled && actionKeys != null) {
                    int left = event.getClientX();
                    int top = event.getClientY();
                    top += Window.getScrollTop();
                    left += Window.getScrollLeft();
                    client.getContextMenu().showAt(this, left, top);
                }
                event.cancelBubble(true);
                event.preventDefault();
            }

            public boolean isSelected() {
                return selected;
            }

            private void toggleSelection() {
                selected = !selected;
                if (selected) {
                    if (selectMode == Table.SELECT_MODE_SINGLE) {
                        deselectAll();
                    }
                    selectedRowKeys.add(String.valueOf(rowKey));
                    addStyleName("v-selected");
                } else {
                    selectedRowKeys.remove(String.valueOf(rowKey));
                    removeStyleName("v-selected");
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.vaadin.terminal.gwt.client.ui.IActionOwner#getActions ()
             */
            public Action[] getActions() {
                if (actionKeys == null) {
                    return new Action[] {};
                }
                final Action[] actions = new Action[actionKeys.length];
                for (int i = 0; i < actions.length; i++) {
                    final String actionKey = actionKeys[i];
                    final TreeAction a = new TreeAction(this, String
                            .valueOf(rowKey), actionKey);
                    a.setCaption(getActionCaption(actionKey));
                    a.setIconUrl(getActionIcon(actionKey));
                    actions[i] = a;
                }
                return actions;
            }

            public ApplicationConnection getClient() {
                return client;
            }

            public String getPaintableId() {
                return paintableId;
            }

            public RenderSpace getAllocatedSpace(Widget child) {
                int w = 0;
                int i = getColIndexOf(child);
                HeaderCell headerCell = tHead.getHeaderCell(i);
                if (headerCell != null) {
                    if (initializedAndAttached) {
                        w = headerCell.getWidth();
                    } else {
                        // header offset width is not absolutely correct value,
                        // but a best guess (expecting similar content in all
                        // columns ->
                        // if one component is relative width so are others)
                        w = headerCell.getOffsetWidth() - getCellExtraWidth();
                    }
                }
                return new RenderSpace(w, 0) {
                    @Override
                    public int getHeight() {
                        return (int) getRowHeight();
                    }
                };
            }

            private int getColIndexOf(Widget child) {
                com.google.gwt.dom.client.Element widgetCell = child
                        .getElement().getParentElement().getParentElement();
                NodeList<TableCellElement> cells = rowElement.getCells();
                for (int i = 0; i < cells.getLength(); i++) {
                    if (cells.getItem(i) == widgetCell) {
                        return i;
                    }
                }
                return -1;
            }

            public boolean hasChildComponent(Widget component) {
                return childWidgets.contains(component);
            }

            public void replaceChildComponent(Widget oldComponent,
                    Widget newComponent) {
                com.google.gwt.dom.client.Element parentElement = oldComponent
                        .getElement().getParentElement();
                int index = childWidgets.indexOf(oldComponent);
                oldComponent.removeFromParent();

                parentElement.appendChild(newComponent.getElement());
                childWidgets.add(index, newComponent);
                adopt(newComponent);

            }

            public boolean requestLayout(Set<Paintable> children) {
                // row size should never change and system wouldn't event
                // survive as this is a kind of fake paitable
                return true;
            }

            public void updateCaption(Paintable component, UIDL uidl) {
                // NOP, not rendered
            }

            public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
                // Should never be called,
                // Component container interface faked here to get layouts
                // render properly
            }
        }
    }

    public void deselectAll() {
        final Object[] keys = selectedRowKeys.toArray();
        for (int i = 0; i < keys.length; i++) {
            final VScrollTableRow row = getRenderedRowByKey((String) keys[i]);
            if (row != null && row.isSelected()) {
                row.toggleSelection();
            }
        }
        // still ensure all selects are removed from (not necessary rendered)
        selectedRowKeys.clear();

    }

    /**
     * Determines the pagelength when the table height is fixed.
     */
    public void updatePageLength() {
        if (scrollBody == null) {
            return;
        }

        if (height == null || height.equals("")) {
            return;
        }

        int rowHeight = (int) scrollBody.getRowHeight();
        int bodyH = bodyContainer.getOffsetHeight();
        int rowsAtOnce = bodyH / rowHeight;
        boolean anotherPartlyVisible = ((bodyH % rowHeight) != 0);
        if (anotherPartlyVisible) {
            rowsAtOnce++;
        }
        if (pageLength != rowsAtOnce) {
            pageLength = rowsAtOnce;
            client.updateVariable(paintableId, "pagelength", pageLength, false);

            if (!rendering) {
                int currentlyVisible = scrollBody.lastRendered
                        - scrollBody.firstRendered;
                if (currentlyVisible < pageLength
                        && currentlyVisible < totalRows) {
                    // shake scrollpanel to fill empty space
                    bodyContainer.setScrollPosition(scrollTop + 1);
                    bodyContainer.setScrollPosition(scrollTop - 1);
                }
            }
        }

    }

    @Override
    public void setWidth(String width) {
        if (this.width.equals(width)) {
            return;
        }

        this.width = width;
        if (width != null && !"".equals(width)) {
            super.setWidth(width);
            int innerPixels = getOffsetWidth() - getBorderWidth();
            if (innerPixels < 0) {
                innerPixels = 0;
            }
            setContentWidth(innerPixels);

            if (!rendering) {
                // readjust undefined width columns
                lazyAdjustColumnWidths.cancel();
                lazyAdjustColumnWidths.schedule(LAZY_COLUMN_ADJUST_TIMEOUT);
            }

        } else {
            super.setWidth("");
        }

    }

    private static final int LAZY_COLUMN_ADJUST_TIMEOUT = 300;

    private final Timer lazyAdjustColumnWidths = new Timer() {
        /**
         * Check for column widths, and available width, to see if we can fix
         * column widths "optimally". Doing this lazily to avoid expensive
         * calculation when resizing is not yet finished.
         */
        @Override
        public void run() {

            Iterator<Widget> headCells = tHead.iterator();
            int usedMinimumWidth = 0;
            int totalExplicitColumnsWidths = 0;
            float expandRatioDivider = 0;
            int colIndex = 0;
            while (headCells.hasNext()) {
                final HeaderCell hCell = (HeaderCell) headCells.next();
                if (hCell.isDefinedWidth()) {
                    totalExplicitColumnsWidths += hCell.getWidth();
                    usedMinimumWidth += hCell.getWidth();
                } else {
                    usedMinimumWidth += hCell.getNaturalColumnWidth(colIndex);
                    expandRatioDivider += hCell.getExpandRatio();
                }
                colIndex++;
            }

            int availW = scrollBody.getAvailableWidth();
            // Hey IE, are you really sure about this?
            availW = scrollBody.getAvailableWidth();
            int visibleCellCount = tHead.getVisibleCellCount();
            availW -= scrollBody.getCellExtraWidth() * visibleCellCount;
            if (willHaveScrollbars()) {
                availW -= Util.getNativeScrollbarSize();
            }

            int extraSpace = availW - usedMinimumWidth;
            if (extraSpace < 0) {
                extraSpace = 0;
            }

            int totalUndefinedNaturaWidths = usedMinimumWidth
                    - totalExplicitColumnsWidths;

            // we have some space that can be divided optimally
            HeaderCell hCell;
            colIndex = 0;
            headCells = tHead.iterator();
            while (headCells.hasNext()) {
                hCell = (HeaderCell) headCells.next();
                if (!hCell.isDefinedWidth()) {
                    int w = hCell.getNaturalColumnWidth(colIndex);
                    int newSpace;
                    if (expandRatioDivider > 0) {
                        // divide excess space by expand ratios
                        newSpace = (int) (w + extraSpace
                                * hCell.getExpandRatio() / expandRatioDivider);
                    } else {
                        if (totalUndefinedNaturaWidths != 0) {
                            // divide relatively to natural column widths
                            newSpace = w + extraSpace * w
                                    / totalUndefinedNaturaWidths;
                        } else {
                            newSpace = w;
                        }
                    }
                    setColWidth(colIndex, newSpace, false);
                }
                colIndex++;
            }
            if ((height == null || "".equals(height))
                    && totalRows == pageLength) {
                // fix body height (may vary if lazy loading is offhorizontal
                // scrollbar appears/disappears)
                int bodyHeight = scrollBody.getRequiredHeight();
                boolean needsSpaceForHorizontalSrollbar = (availW < usedMinimumWidth);
                if (needsSpaceForHorizontalSrollbar) {
                    bodyHeight += Util.getNativeScrollbarSize();
                }
                int heightBefore = getOffsetHeight();
                bodyContainer.setHeight(bodyHeight + "px");
                if (heightBefore != getOffsetHeight()) {
                    Util.notifyParentOfSizeChange(VScrollTable.this, false);
                }
            }
            scrollBody.reLayoutComponents();
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    Util.runWebkitOverflowAutoFix(bodyContainer.getElement());
                }
            });
        }
    };

    /**
     * helper to set pixel size of head and body part
     * 
     * @param pixels
     */
    private void setContentWidth(int pixels) {
        tHead.setWidth(pixels + "px");
        bodyContainer.setWidth(pixels + "px");
    }

    private int borderWidth = -1;

    /**
     * @return border left + border right
     */
    private int getBorderWidth() {
        if (borderWidth < 0) {
            borderWidth = Util.measureHorizontalPaddingAndBorder(bodyContainer
                    .getElement(), 2);
            if (borderWidth < 0) {
                borderWidth = 0;
            }
        }
        return borderWidth;
    }

    /**
     * Ensures scrollable area is properly sized. This method is used when fixed
     * size is used.
     */
    private void setContainerHeight() {
        if (height != null && !"".equals(height)) {
            int contentH = getOffsetHeight() - tHead.getOffsetHeight();
            contentH -= getContentAreaBorderHeight();
            if (contentH < 0) {
                contentH = 0;
            }
            bodyContainer.setHeight(contentH + "px");
        }
    }

    private int contentAreaBorderHeight = -1;
    private int scrollLeft;
    private int scrollTop;

    /**
     * @return border top + border bottom of the scrollable area of table
     */
    private int getContentAreaBorderHeight() {
        if (contentAreaBorderHeight < 0) {
            if (BrowserInfo.get().isIE7()) {
                contentAreaBorderHeight = Util
                        .measureVerticalBorder(bodyContainer.getElement());
            } else {
                DOM.setStyleAttribute(bodyContainer.getElement(), "overflow",
                        "hidden");
                int oh = bodyContainer.getOffsetHeight();
                int ch = bodyContainer.getElement().getPropertyInt(
                        "clientHeight");
                contentAreaBorderHeight = oh - ch;
                DOM.setStyleAttribute(bodyContainer.getElement(), "overflow",
                        "auto");
            }
        }
        return contentAreaBorderHeight;
    }

    @Override
    public void setHeight(String height) {
        this.height = height;
        super.setHeight(height);
        setContainerHeight();
        if (initializedAndAttached) {
            updatePageLength();
        }
        if (!rendering) {
            // Webkit may sometimes get an odd rendering bug (white space
            // between header and body), see bug #3875. Running
            // overflow hack here to shake body element a bit.
            Util.runWebkitOverflowAutoFix(bodyContainer.getElement());
        }
    }

    /*
     * Overridden due Table might not survive of visibility change (scroll pos
     * lost). Example ITabPanel just set contained components invisible and back
     * when changing tabs.
     */
    @Override
    public void setVisible(boolean visible) {
        if (isVisible() != visible) {
            super.setVisible(visible);
            if (initializedAndAttached) {
                if (visible) {
                    DeferredCommand.addCommand(new Command() {
                        public void execute() {
                            bodyContainer
                                    .setScrollPosition((int) (firstRowInViewPort * scrollBody
                                            .getRowHeight()));
                        }
                    });
                }
            }
        }
    }

    /**
     * Helper function to build html snippet for column or row headers
     * 
     * @param uidl
     *            possibly with values caption and icon
     * @return html snippet containing possibly an icon + caption text
     */
    private String buildCaptionHtmlSnippet(UIDL uidl) {
        String s = uidl.getStringAttribute("caption");
        if (uidl.hasAttribute("icon")) {
            s = "<img src=\""
                    + client
                            .translateVaadinUri(uidl.getStringAttribute("icon"))
                    + "\" alt=\"icon\" class=\"v-icon\">" + s;
        }
        return s;
    }

    /**
     * This method has logic which rows needs to be requested from server when
     * user scrolls
     */
    public void onScroll(ScrollEvent event) {
        scrollLeft = bodyContainer.getElement().getScrollLeft();
        scrollTop = bodyContainer.getScrollPosition();
        if (!initializedAndAttached) {
            return;
        }
        if (!enabled) {
            bodyContainer
                    .setScrollPosition((int) (firstRowInViewPort * scrollBody
                            .getRowHeight()));
            return;
        }

        rowRequestHandler.cancel();

        if (BrowserInfo.get().isSafari() && event != null && scrollTop == 0) {
            // due to the webkitoverflowworkaround, top may sometimes report 0
            // for webkit, although it really is not. Expecting to have the
            // correct
            // value available soon.
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    onScroll(null);
                }
            });
            return;
        }

        // fix headers horizontal scrolling
        tHead.setHorizontalScrollPosition(scrollLeft);

        firstRowInViewPort = (int) Math.ceil(scrollTop
                / scrollBody.getRowHeight());
        if (firstRowInViewPort > totalRows - pageLength) {
            firstRowInViewPort = totalRows - pageLength;
        }

        int postLimit = (int) (firstRowInViewPort + (pageLength - 1) + pageLength
                * cache_react_rate);
        if (postLimit > totalRows - 1) {
            postLimit = totalRows - 1;
        }
        int preLimit = (int) (firstRowInViewPort - pageLength
                * cache_react_rate);
        if (preLimit < 0) {
            preLimit = 0;
        }
        final int lastRendered = scrollBody.getLastRendered();
        final int firstRendered = scrollBody.getFirstRendered();

        if (postLimit <= lastRendered && preLimit >= firstRendered) {
            // remember which firstvisible we requested, in case the server has
            // a differing opinion
            lastRequestedFirstvisible = firstRowInViewPort;
            client.updateVariable(paintableId, "firstvisible",
                    firstRowInViewPort, false);
            return; // scrolled withing "non-react area"
        }

        if (firstRowInViewPort - pageLength * cache_rate > lastRendered
                || firstRowInViewPort + pageLength + pageLength * cache_rate < firstRendered) {
            // need a totally new set
            rowRequestHandler
                    .setReqFirstRow((firstRowInViewPort - (int) (pageLength * cache_rate)));
            int last = firstRowInViewPort + (int) (cache_rate * pageLength)
                    + pageLength - 1;
            if (last >= totalRows) {
                last = totalRows - 1;
            }
            rowRequestHandler.setReqRows(last
                    - rowRequestHandler.getReqFirstRow() + 1);
            rowRequestHandler.deferRowFetch();
            return;
        }
        if (preLimit < firstRendered) {
            // need some rows to the beginning of the rendered area
            rowRequestHandler
                    .setReqFirstRow((int) (firstRowInViewPort - pageLength
                            * cache_rate));
            rowRequestHandler.setReqRows(firstRendered
                    - rowRequestHandler.getReqFirstRow());
            rowRequestHandler.deferRowFetch();

            return;
        }
        if (postLimit > lastRendered) {
            // need some rows to the end of the rendered area
            rowRequestHandler.setReqFirstRow(lastRendered + 1);
            rowRequestHandler.setReqRows((int) ((firstRowInViewPort
                    + pageLength + pageLength * cache_rate) - lastRendered));
            rowRequestHandler.deferRowFetch();
        }
    }

}
