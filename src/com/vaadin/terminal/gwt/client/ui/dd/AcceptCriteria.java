package com.vaadin.terminal.gwt.client.ui.dd;

import com.vaadin.terminal.gwt.client.UIDL;

public interface AcceptCriteria {

    public boolean accept(Transferable transferable, UIDL configuration);

}
