package com.vaadin.terminal.gwt.server;

import java.io.IOException;
import java.io.Serializable;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import com.vaadin.external.org.apache.commons.fileupload.FileUploadException;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.Paintable.RepaintRequestEvent;

@SuppressWarnings("serial")
public class PortletCommunicationManagerImpl implements
        PortletCommunicationManager, Paintable.RepaintRequestListener, Serializable {

    public void repaintRequested(RepaintRequestEvent event) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void handleFileUpload(ActionRequest request, ActionResponse response)
            throws FileUploadException, IOException {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void handleUIDLRequest(ActionRequest request, ActionResponse response)
            throws IOException, PortletException {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
