package org.agilewiki.pactor.utilImpl.durable.incDes;

import org.agilewiki.pactor.util.durable.FactoryLocator;
import org.agilewiki.pactor.util.durable.incDes.IncDes;
import org.agilewiki.pactor.utilImpl.durable.FactoryImpl;

/**
 * Creates a IncDesImpl.
 */
public class IncDesFactory extends FactoryImpl {

    public static void registerFactory(FactoryLocator factoryLocator) {
        factoryLocator.registerFactory(new IncDesFactory());
    }

    /**
     * Create a JLPCActorFactory.
     */
    protected IncDesFactory() {
        super(IncDes.FACTORY_NAME);
    }

    /**
     * Create a JLPCActor.
     *
     * @return The new actor.
     */
    @Override
    protected IncDesImpl instantiateActor() {
        return new IncDesImpl();
    }
}