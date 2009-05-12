package com.vaadin.demo.sampler.features.panels;

import com.vaadin.demo.sampler.APIResource;
import com.vaadin.demo.sampler.Feature;
import com.vaadin.demo.sampler.FeatureSet;
import com.vaadin.demo.sampler.NamedExternalResource;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;

public class PanelLight extends Feature {
    @Override
    public String getName() {
        return "Panel, light style";
    }

    @Override
    public String getDescription() {
        return "The 'light' panel has less decorations than the regular Panel style.";

    }

    @Override
    public APIResource[] getRelatedAPI() {
        return new APIResource[] { new APIResource(Panel.class),
                new APIResource(Layout.class) };
    }

    @Override
    public Class[] getRelatedFeatures() {
        return new Class[] { PanelBasic.class, FeatureSet.Layouts.class };
    }

    @Override
    public NamedExternalResource[] getRelatedResources() {
        // TODO Auto-generated method stub
        return null;
    }

}