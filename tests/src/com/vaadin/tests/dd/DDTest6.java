package com.vaadin.tests.dd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.event.Action;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIsTarget;
import com.vaadin.event.dd.acceptcriteria.Not;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.tests.components.TestBase;
import com.vaadin.tests.util.TestUtils;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.AbsoluteLayout.ComponentPosition;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable.Html5File;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.Upload.Receiver;

public class DDTest6 extends TestBase {

    java.util.Random r = new java.util.Random(1);

    File[] files = new File[] { new Folder("Docs"), new Folder("Music"),
            new Folder("Images"), new File("document.doc"),
            new File("song.mp3"), new File("photo.jpg") };

    private DropHandler dh;

    private static Tree tree1;

    private SplitPanel sp;

    private BeanItemContainer<File> fs1;

    private static int count;

    private static DDTest6 instance;

    @Override
    protected void setup() {
        instance = this; // Note, test only works with single app per server if
        // get()
        // not converted to thread local

        sp = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
        sp.setSplitPosition(20);
        CssLayout l = new CssLayout();
        sp.setFirstComponent(l);

        tree1 = new Tree("Volume 1");
        tree1.setImmediate(true);

        fs1 = new BeanItemContainer<File>(File.class);
        tree1.setContainerDataSource(fs1);
        for (int i = 0; i < files.length; i++) {
            fs1.addBean(files[i]);
            if (files[i] instanceof Folder) {
                tree1.setChildrenAllowed(files[i], true);
            } else {
                tree1.setChildrenAllowed(files[i], false);
            }
            if (i >= files.length / 2) {
                tree1.setParent(files[i], files[i - files.length / 2]);
            }
        }
        tree1.setItemCaptionPropertyId("name");
        tree1.setItemIconPropertyId("icon");

        tree1.setDragMode(TreeDragMode.NODE);

        DropHandler dropHandler = new DropHandler() {
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

            public void drop(DragAndDropEvent dropEvent) {
                File file = null;
                Folder folder = null;
                TreeTargetDetails dropTargetData = (TreeTargetDetails) dropEvent
                        .getDropTargetDetails();
                folder = (Folder) dropTargetData.getItemIdInto();
                if (dropEvent.getTransferable() instanceof DataBoundTransferable) {
                    DataBoundTransferable transferable = (DataBoundTransferable) dropEvent
                            .getTransferable();
                    file = (File) transferable.getItemId();
                } else if (dropEvent.getTransferable().getSourceComponent() instanceof FileIcon) {
                    FileIcon draggedIcon = (FileIcon) dropEvent
                            .getTransferable().getSourceComponent();
                    file = draggedIcon.file;

                }
                setParent(file, folder);
            }
        };

        tree1.setDropHandler(dropHandler);

        Handler actionHandler = new Handler() {

            private Action[] actions = new Action[] { new Action("Remove") };

            public void handleAction(Action action, Object sender, Object target) {
                ContainerHierarchicalWrapper containerDataSource = (ContainerHierarchicalWrapper) tree1
                        .getContainerDataSource();
                containerDataSource.removeItemRecursively(target);
            }

            public Action[] getActions(Object target, Object sender) {
                return actions;
            }
        };
        tree1.addActionHandler(actionHandler);

        tree1.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                Object value = event.getProperty().getValue();
                if (value != null && !(value instanceof Folder)) {
                    value = tree1.getParent(value);
                }
                FolderView folderView = FolderView.get((Folder) value);
                sp.setSecondComponent(folderView);
                folderView.reload();
            }
        });

        l.addComponent(tree1);

        sp.setSecondComponent(FolderView.get(null));

        getLayout().setSizeFull();
        getLayout().addComponent(sp);
        TestUtils
                .injectCSS(
                        getLayout().getWindow(),
                        ""
                                + ".v-tree .v-icon {height:16px;} "
                                + ".v-tree-node-caption-drag-top {/*border-top: none;*/} "
                                + ".v-tree-node-caption-drag-bottom {border-bottom: none ;} "
                                + ".v-tree-node-caption-drag-center {background-color: transparent;}"
                                + ".v-tree-node-caption-dragfolder { background-color: cyan;} ");

    }

    private final static ThemeResource FOLDER = new ThemeResource(
            "../runo/icons/64/folder.png");
    private final static ThemeResource DOC = new ThemeResource(
            "../runo/icons/64/document.png");

    public static class File {
        private Resource icon = DOC;
        private String name;
        private ByteArrayOutputStream bas;
        private String type;

        public File(String fileName) {
            name = fileName;
        }

        public File(String fileName, ByteArrayOutputStream bas) {
            this(fileName);
            this.bas = bas;
        }

        public void setIcon(Resource icon) {
            this.icon = icon;
        }

        public Resource getIcon() {
            return icon;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public Resource getResource() {
            StreamSource streamSource = new StreamSource() {
                public InputStream getStream() {
                    if (bas != null) {
                        byte[] byteArray = bas.toByteArray();
                        return new ByteArrayInputStream(byteArray);
                    }
                    // TODO Auto-generated method stub
                    return null;
                }
            };
            return new StreamResource(streamSource, getName(), DDTest6.get());
        }
    }

    public static class Folder extends File {

        public Folder(String fileName) {
            super(fileName);
            setIcon(FOLDER);
        }

    }

    @Override
    protected String getDescription() {
        return "dd: tree and web desktop tests. FF36 supports draggin files from client side. (try dragging png image + double click) TODO more files, auto-opening folders";
    }

    @Override
    protected Integer getTicketNumber() {
        return 119;
    }

    private void openFile(File file) {
        // ATM supports only images.
        if (file.getType().equals("image/png")) {
            Embedded embedded = new Embedded(file.getName(), file.getResource());
            Window w = new Window(file.getName());
            w.addComponent(embedded);
            w.getContent().setSizeUndefined();
            getMainWindow().addWindow(w);
        } else if (file.getType().equals("text/csv")) {
            showSpreadsheet(file);
        }
    }

    private void showSpreadsheet(File file) {
        ApplicationResource resource = (ApplicationResource) file.getResource();
        String string = new String(file.bas.toByteArray());
        String[] rows = string.split("\n");
        String[] cols = rows[0].split(",");
        Table table = new Table();
        for (String string2 : cols) {
            String col = string2.replaceAll("\"", ""); // remove surrounding ""
            table.addContainerProperty(string2, String.class, "");
        }
        for (int i = 1; i < rows.length; i++) {
            String[] split = rows[i].split(",");
            table.addItem(split, "" + i);
        }
        Window w = new Window(file.getName());
        w.getContent().setSizeUndefined();
        table.setEditable(true);
        w.addComponent(table);
        getMainWindow().addWindow(w);
        // TODO saving would be nice demo

    }

    static class FolderView extends DragAndDropWrapper implements DropHandler {

        static final HashMap<Folder, FolderView> views = new HashMap<Folder, FolderView>();

        public static FolderView get(Folder f) {

            FolderView folder2 = views.get(f);
            if (folder2 == null) {
                folder2 = new FolderView(f);
                views.put(f, folder2);
            }
            return folder2;
        }

        private Folder folder;
        private AbsoluteLayout l;
        private int x;
        private int y;

        private FolderView(Folder f) {
            super(new AbsoluteLayout());
            l = (AbsoluteLayout) getCompositionRoot();
            setSizeFull();
            l.setSizeFull();
            folder = f;

            setDropHandler(this);
        }

        @Override
        public void attach() {
            reload();
            super.attach();
        }

        void reload() {
            Collection<?> children = folder == null ? DDTest6.get().tree1
                    .rootItemIds() : DDTest6.get().tree1.getChildren(folder);
            if (children == null) {
                l.removeAllComponents();
                return;
            } else {
                // make modifiable
                children = new HashSet<Object>(children);
            }
            Set<Component> removed = new HashSet<Component>();
            for (Iterator<Component> componentIterator = l
                    .getComponentIterator(); componentIterator.hasNext();) {
                FileIcon next = (FileIcon) componentIterator.next();
                if (!children.contains(next.file)) {
                    removed.add(next);
                } else {
                    children.remove(next.file);
                }
            }

            for (Component component : removed) {
                l.removeComponent(component);
            }

            for (Object object : children) {
                FileIcon fileIcon = new FileIcon((File) object);
                l.addComponent(fileIcon);
                ComponentPosition position = l.getPosition(fileIcon);
                position.setTop((y++ / 5) % 5 * 100, UNITS_PIXELS);
                position.setLeft(x++ % 5 * 100, UNITS_PIXELS);
            }

        }

        public void drop(DragAndDropEvent dropEvent) {

            if (dropEvent.getTransferable().getSourceComponent() instanceof FileIcon) {
                // update the position

                DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) dropEvent
                        .getTransferable();
                MouseEventDetails mouseDownEvent = transferable
                        .getMouseDownEvent();

                WrapperTargetDetails dropTargetDetails = (WrapperTargetDetails) dropEvent
                        .getDropTargetDetails();
                MouseEventDetails mouseEvent = dropTargetDetails
                        .getMouseEvent();

                int deltaX = mouseEvent.getClientX()
                        - mouseDownEvent.getClientX();
                int deltaY = mouseEvent.getClientY()
                        - mouseDownEvent.getClientY();

                ComponentPosition position = l.getPosition(transferable
                        .getSourceComponent());
                position.setTop(position.getTopValue() + deltaY, UNITS_PIXELS);
                position
                        .setLeft(position.getLeftValue() + deltaX, UNITS_PIXELS);

            } else if (dropEvent.getTransferable().getSourceComponent() == tree1) {

                // dragged something from tree to the folder shown

                File draggedFile = (File) ((DataBoundTransferable) dropEvent
                        .getTransferable()).getItemId();
                DDTest6.get().setParent(draggedFile, folder);
            } else {
                // expecting this to be an html5 drag
                WrapperTransferable tr = (WrapperTransferable) dropEvent
                        .getTransferable();
                Html5File[] files2 = tr.getFiles();
                if (files2 != null) {
                    for (Html5File html5File : files2) {
                        String fileName = html5File.getFileName();
                        // int bytes = html5File.getFileSize();
                        final ByteArrayOutputStream bas = new ByteArrayOutputStream() {
                            @Override
                            public void close() throws IOException {
                                super.close();
                            }
                        };

                        Receiver receiver = new Receiver() {
                            public OutputStream receiveUpload(String filename,
                                    String MIMEType) {
                                return bas;
                            }
                        };

                        html5File.setReceiver(receiver);

                        File file = new File(fileName, bas);
                        file.setType(html5File.getType());
                        // FF don't know csv
                        if (fileName.endsWith(".csv")) {
                            file.setType("text/csv");
                        }
                        DDTest6.get().fs1.addBean(file);
                        DDTest6.get().tree1.setChildrenAllowed(file, false);
                        DDTest6.get().setParent(file, folder);
                    }

                }

            }
        }

        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }

    }

    static class FileIcon extends DragAndDropWrapper {
        private final File file;
        private CssLayout l;

        public FileIcon(final File file) {
            super(new CssLayout());
            l = (CssLayout) getCompositionRoot();
            setWidth(null);
            l.setWidth(null);
            setDragStartMode(DragStartMode.WRAPPER); // drag all contained
            // components, not just the
            // one on it started
            this.file = file;
            Resource icon2 = file.getIcon();
            String name = file.getName();
            l.addComponent(new Embedded(null, icon2));
            l.addComponent(new Label(name));

            l.addListener(new LayoutClickListener() {
                public void layoutClick(LayoutClickEvent event) {
                    if (event.isDoubleClick()) {
                        if (file instanceof Folder) {
                            get().tree1.setValue(file);
                        } else {
                            String type = file.getType();
                            if (canDisplay(type)) {
                                DDTest6.get().openFile(file);
                            }
                        }
                    }

                }

                String[] knownTypes = new String[] { "image/png", "text/csv" };

                private boolean canDisplay(String type) {
                    if (type != null) {
                        for (String t : knownTypes) {
                            if (t.equals(type)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

            if (file instanceof Folder) {

                setDropHandler(new DropHandler() {

                    public AcceptCriterion getAcceptCriterion() {
                        return new Not(SourceIsTarget.get());
                    }

                    public void drop(DragAndDropEvent dropEvent) {
                        File f = null;

                        if (dropEvent.getTransferable().getSourceComponent() instanceof FileIcon) {
                            FileIcon new_name = (FileIcon) dropEvent
                                    .getTransferable().getSourceComponent();
                            f = new_name.file;
                        } else if (dropEvent.getTransferable()
                                .getSourceComponent() == tree1) {
                            f = (File) ((DataBoundTransferable) dropEvent
                                    .getTransferable()).getItemId();
                        }

                        if (f != null) {
                            get().setParent(f, (Folder) FileIcon.this.file);
                        }

                    }
                });

            }
        }
    }

    static DDTest6 get() {
        return instance;
    }

    public void setParent(File file, Folder newParent) {
        tree1.setParent(file, newParent);
        if (sp.getSecondComponent() instanceof FolderView) {
            FolderView view = (FolderView) sp.getSecondComponent();
            view.reload();
        }
    }
}
