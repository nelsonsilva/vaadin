/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.tests.book;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.itmill.toolkit.data.Property;
import com.itmill.toolkit.data.Property.ValueChangeEvent;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.CheckBox;
import com.itmill.toolkit.ui.CustomComponent;
import com.itmill.toolkit.ui.Label;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.RichTextArea;
import com.itmill.toolkit.ui.Table;
import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.Button.ClickEvent;

public class TableEditable extends CustomComponent {
    /* A layout needed for the example. */
    OrderedLayout layout = new OrderedLayout(OrderedLayout.ORIENTATION_VERTICAL);
    
    TableEditable() {
        setCompositionRoot(layout);

        // Create a table. It is by default not editable.
        final Table table = new Table();
        
        // Define the names and data types of columns.
        table.addContainerProperty("Date",     Date.class,  null);
        table.addContainerProperty("Work",     Boolean.class, null);
        table.addContainerProperty("Comments", String.class,  null);
        
        // Add a few items in the table.
        for (int i=0; i<100; i++) {
            Calendar calendar = new GregorianCalendar(2008,0,1);
            calendar.add(Calendar.DAY_OF_YEAR, i);
            
            // Create the table row.
            table.addItem(new Object[] {calendar.getTime(),
                                        new Boolean(false),
                                        ""},
                          new Integer(i)); // Item identifier
        }
        
        table.setPageLength(8);
        layout.addComponent(table);
        
        final CheckBox switchEditable = new CheckBox("Editable");
        switchEditable.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                table.setEditable(((Boolean)event.getProperty().getValue()).booleanValue());
            }
        });
        switchEditable.setImmediate(true);
        layout.addComponent(switchEditable);
    }
}
