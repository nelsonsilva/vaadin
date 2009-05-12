package com.vaadin.demo.sampler.features.table;

import com.vaadin.demo.sampler.APIResource;
import com.vaadin.demo.sampler.Feature;
import com.vaadin.demo.sampler.FeatureSet;
import com.vaadin.demo.sampler.NamedExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

public class TableColumnHeaders extends Feature {

    @Override
    public String getName() {
        return "Table, column headers";
    }

    @Override
    public Component getExample() {
        return new TableMainFeaturesExample();
    }

    @Override
    public String getDescription() {
        return "A Table can have column headers, which support different modes"
                + " with automatic or explicitly set caption and/or icon.";
    }

    @Override
    public APIResource[] getRelatedAPI() {
        return new APIResource[] { new APIResource(Table.class) };
    }

    @Override
    public Class[] getRelatedFeatures() {
        return new Class[] { FeatureSet.Tables.class };
    }

    @Override
    public NamedExternalResource[] getRelatedResources() {
        return null;
    }

}