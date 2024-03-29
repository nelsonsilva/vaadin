/*
@VaadinApache2LicenseForJavaFiles@
 */
/**
 * 
 */
package com.vaadin.event.dd.acceptcriteria;

import com.vaadin.event.TransferableImpl;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.dd.VDragSourceIs;
import com.vaadin.ui.Component;

/**
 * Client side criteria that checks if the drag source is one of the given
 * components.
 * 
 * @since 6.3
 */
@SuppressWarnings("serial")
@ClientCriterion(VDragSourceIs.class)
public class SourceIs extends ClientSideCriterion {

    private Component[] component;

    public SourceIs(Component... component) {
        this.component = component;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("c", component.length);
        for (int i = 0; i < component.length; i++) {
            target.addAttribute("component" + i, component[i]);
        }
    }

    public boolean accept(DragAndDropEvent dragEvent) {
        if (dragEvent.getTransferable() instanceof TransferableImpl) {
            Component sourceComponent = ((TransferableImpl) dragEvent
                    .getTransferable()).getSourceComponent();
            for (Component c : component) {
                if (c == sourceComponent) {
                    return true;
                }
            }
        }

        return false;
    }

}