package org.variantsync.vevos.simulation.feature;

import de.ovgu.featureide.fm.core.base.*;
import de.ovgu.featureide.fm.core.base.event.FeatureIDEEvent;
import de.ovgu.featureide.fm.core.base.event.IEventListener;

import java.util.List;

public class SimpleFeature implements IFeature {
    private String name;

    public SimpleFeature(String name) {
        this.name = name;
    }

    @Override
    public IFeature clone(IFeatureModel iFeatureModel, IFeatureStructure iFeatureStructure) {
        return new SimpleFeature(this.name);
    }

    @Override
    public IFeatureProperty getProperty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IFeatureStructure getStructure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createTooltip(Object... objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IFeatureModel getFeatureModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getInternalId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String s) {
        this.name = s;
    }

    @Override
    public IPropertyContainer getCustomProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(IEventListener iEventListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fireEvent(FeatureIDEEvent featureIDEEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(IEventListener iEventListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IEventListener> getListeners() {
        throw new UnsupportedOperationException();
    }
}
