package com.vaadin.demo.sampler.features.commons;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class IconsExample extends VerticalLayout {

    public IconsExample() {
        setSpacing(true);

        /* Button w/ icon */
        Button button = new Button("Save");
        button.setIcon(new ThemeResource("icons/action_save.gif"));
        addComponent(button);

        /* Label */;
        Label l = new Label("Icons are very handy");
        l.setCaption("Comment");
        l.setIcon(new ThemeResource("icons/comment_yellow.gif"));
        addComponent(l);

        /* Panel w/ links */
        Panel p = new Panel("Handy links");
        p.setIcon(new ThemeResource("icons/icon_info.gif"));
        addComponent(p);
        Link lnk = new Link("http://www.itmill.com", new ExternalResource(
                "http://www.itmill.com"));
        lnk.setIcon(new ThemeResource("icons/icon_world.gif"));
        p.addComponent(lnk);
        lnk = new Link("http://www.itmill.com/developers/",
                new ExternalResource("http://www.itmill.com/developers/"));
        lnk.setIcon(new ThemeResource("icons/icon_world.gif"));
        p.addComponent(lnk);
        lnk = new Link("http://dev.itmill.com/", new ExternalResource(
                "http://dev.itmill.com/"));
        lnk.setIcon(new ThemeResource("icons/icon_world.gif"));
        p.addComponent(lnk);
        lnk = new Link("http://forum.itmill.com", new ExternalResource(
                "http://forum.itmill.com"));
        lnk.setIcon(new ThemeResource("icons/icon_world.gif"));
        p.addComponent(lnk);

    }
}