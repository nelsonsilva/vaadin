package com.vaadin.demo.sampler.features.commons;

import com.vaadin.demo.sampler.APIResource;
import com.vaadin.demo.sampler.Feature;
import com.vaadin.demo.sampler.NamedExternalResource;
import com.vaadin.ui.AbstractComponent;

public class Tooltips extends Feature {

    @Override
    public String getName() {
        return "Tooltips";
    }

    @Override
    public String getDescription() {
        return "Most components can have a <i>description</i>,"
                + " which is shown as a <i>\"tooltip\"</i>."
                + " Descriptions may have formatted ('rich') content.<br/>"
                + "";
    }

    @Override
    public APIResource[] getRelatedAPI() {
        return new APIResource[] { new APIResource(AbstractComponent.class) };
    }

    @Override
    public Class[] getRelatedFeatures() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NamedExternalResource[] getRelatedResources() {
        // TODO Auto-generated method stub
        return null;
    }

}