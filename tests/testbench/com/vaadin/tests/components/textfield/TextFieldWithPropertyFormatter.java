package com.vaadin.tests.components.textfield;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TextField;

public class TextFieldWithPropertyFormatter extends TestBase {

    private PropertyFormatter<BigDecimal> formatter;
    private Property<BigDecimal> property;

    @Override
    protected void setup() {
        /*
         * Formatter that: - formats in UK currency style - scales to 2 fraction
         * digits - rounds half up
         */
        // Property containing BigDecimal
        property = new Property<BigDecimal>() {
            private BigDecimal value;

            public BigDecimal getValue() {
                return value;
            }

            public void setValue(Object newValue) throws ReadOnlyException {
                if (newValue == null) {
                    value = null;
                } else if (newValue instanceof BigDecimal) {
                    value = (BigDecimal) newValue;
                } else {
                    throw new IllegalArgumentException(
                            "Value must be of type BigDecimal");
                }
            }

            public Class<BigDecimal> getType() {
                return BigDecimal.class;
            }

            public boolean isReadOnly() {
                return false;
            }

            public void setReadOnly(boolean newStatus) {
                // ignore
            }
        };

        formatter = new PropertyFormatter<BigDecimal>(property) {

            private final DecimalFormat df = new DecimalFormat("#,##0.00",
                    new DecimalFormatSymbols(new Locale("en", "UK")));
            {
                df.setParseBigDecimal(true);
                // df.setRoundingMode(RoundingMode.HALF_UP);
            }

            @Override
            public String format(BigDecimal value) {

                final String retVal;
                if (value == null) {
                    retVal = "";
                } else {
                    retVal = df.format(value);
                }
                return retVal;
            }

            @Override
            public BigDecimal parse(String formattedValue) throws Exception {
                if (formattedValue != null
                        && formattedValue.trim().length() != 0) {
                    BigDecimal value = (BigDecimal) df.parse(formattedValue);
                    value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
                    return value;
                }
                return null;
            }
        };

        final TextField tf1 = new TextField();

        tf1.setPropertyDataSource(formatter);

        addComponent(tf1);

        Button b = new Button(
                "Sync (typing 12345.6789 and clicking this should format field)");
        b.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
            }
        });
        addComponent(b);
        b = new Button("Set '12345.6789' to textfield on the server side");
        b.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                tf1.setValue("12345.6789");
            }
        });
        addComponent(b);

    }

    @Override
    protected String getDescription() {
        return "Should work";
    }

    @Override
    protected Integer getTicketNumber() {
        return 4394;
    }

}
