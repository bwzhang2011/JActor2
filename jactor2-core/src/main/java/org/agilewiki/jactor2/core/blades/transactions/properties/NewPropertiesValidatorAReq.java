package org.agilewiki.jactor2.core.blades.transactions.properties;

import java.util.NavigableMap;
import java.util.SortedMap;

import org.agilewiki.jactor2.core.blades.transactions.NewValidatorAReq;
import org.agilewiki.jactor2.core.reactors.NonBlockingReactor;

abstract public class NewPropertiesValidatorAReq
        extends
        NewValidatorAReq<NavigableMap<String, Object>, PropertiesWrapper, PropertyChanges, SortedMap<String, Object>> {
    public NewPropertiesValidatorAReq(final NonBlockingReactor _targetReactor,
            final PropertiesBlade _propertiesBlade, final String _prefix) {
        super(_targetReactor, _propertiesBlade.getPropertiesProcessor(),
                _prefix);
    }
}