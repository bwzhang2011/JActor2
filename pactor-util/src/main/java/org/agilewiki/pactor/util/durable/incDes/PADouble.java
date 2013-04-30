package org.agilewiki.pactor.util.durable.incDes;

import org.agilewiki.pactor.api.Request;

public interface PADouble extends IncDes {

    public static final String FACTORY_NAME = "double";

    Request<Double> getValueReq();

    Double getValue();

    Request<Void> setValueReq(final Double _v);

    void setValue(final Double _v);
}