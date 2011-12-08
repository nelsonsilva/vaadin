package com.vaadin.tests.components.root;

import com.vaadin.RootRequiresMoreInformation;
import com.vaadin.annotations.RootInitRequiresBrowserDetals;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.WrappedRequest.BrowserDetails;
import com.vaadin.tests.components.AbstractTestApplication;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Root;
import com.vaadin.ui.VerticalLayout;

public class LazyInitRoots extends AbstractTestApplication {

    @RootInitRequiresBrowserDetals
    private static class LazyInitRoot extends Root {
        @Override
        public void init(WrappedRequest request) {
            BrowserDetails browserDetails = request.getBrowserDetails();
            getContent().addComponent(
                    new Label("Lazy init root: "
                            + browserDetails.getUriFragmet()));
        }
    }

    @Override
    public Root getRoot(WrappedRequest request)
            throws RootRequiresMoreInformation {
        if (request.getParameter("lazyCreate") != null) {
            BrowserDetails browserDetails = request.getBrowserDetails();
            if (browserDetails == null) {
                throw new RootRequiresMoreInformation();
            } else {
                Root root = new Root();
                root.getContent().addComponent(
                        new Label("Lazy create root: "
                                + browserDetails.getUriFragmet()));
                return root;
            }
        } else if (request.getParameter("lazyInit") != null) {
            return new LazyInitRoot();
        } else {
            VerticalLayout content = new VerticalLayout();
            Link lazyCreateLink = new Link("Open lazyCreate root",
                    new ExternalResource(getURL() + "?lazyCreate#lazyCreate"));
            lazyCreateLink.setTargetName("_blank");
            content.addComponent(lazyCreateLink);

            Link lazyInitLink = new Link("Open lazyInit root",
                    new ExternalResource(getURL() + "?lazyInit#lazyInit"));
            lazyInitLink.setTargetName("_blank");
            content.addComponent(lazyInitLink);

            return new Root(content);
        }
    }

    @Override
    protected String getTestDescription() {
        return "BrowserDetails should be available in Application.getRoot if RootRequiresMoreInformation has been thrown and in Root.init if the root has the @RootInitRequiresBrowserDetals annotation";
    }

    @Override
    protected Integer getTicketNumber() {
        return Integer.valueOf(7883);
    }

}