package com.itmill.toolkit.demo.sampler.features.trees;

import com.itmill.toolkit.demo.sampler.APIResource;
import com.itmill.toolkit.demo.sampler.Feature;
import com.itmill.toolkit.demo.sampler.NamedExternalResource;
import com.itmill.toolkit.ui.Tree;

public class TreeMultiSelect extends Feature {
    @Override
    public String getName() {
        return "Tree, multiple selections";
    }

    @Override
    public String getDescription() {
        return "The Tree component allows a natural way to represent"
                + " data that has hierarchical relationships, such as"
                + " filesystems or message threads."
                + "<br>In this example, you can select multiple tree nodes"
                + " and delete your selection. Click a selected item again to de-select it.";
    }

    @Override
    public APIResource[] getRelatedAPI() {
        return new APIResource[] { new APIResource(Tree.class) };
    }

    @Override
    public Class[] getRelatedFeatures() {
        return new Class[] { TreeSingleSelect.class, TreeActions.class,
                TreeMouseEvents.class };
    }

    @Override
    public NamedExternalResource[] getRelatedResources() {
        // TODO Auto-generated method stub
        return null;
    }
}
