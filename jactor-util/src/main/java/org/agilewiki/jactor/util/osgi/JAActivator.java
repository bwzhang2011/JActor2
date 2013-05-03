package org.agilewiki.jactor.util.osgi;

import org.agilewiki.jactor.api.MailboxFactory;
import org.agilewiki.jactor.impl.DefaultMailboxFactoryImpl;
import org.agilewiki.jactor.util.JAProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JAActivator implements BundleActivator, AutoCloseable {
    private MailboxFactory mailboxFactory;
    private JAProperties jaProperties;
    protected BundleContext bundleContext;
    private boolean closing;

    protected MailboxFactory getMailboxFactory() {
        return mailboxFactory;
    }

    protected void createMailboxFactory() throws Exception {
        mailboxFactory = new DefaultMailboxFactoryImpl();
        mailboxFactory.addAutoClosable(this);
        jaProperties = new JAProperties(mailboxFactory, null);
        jaProperties.putProperty("bundleContext", bundleContext);
    }

    protected boolean isBundleClosing() {
        return closing;
    }

    @Override
    public void start(final BundleContext _context) throws Exception {
        bundleContext = _context;
        createMailboxFactory();
    }

    @Override
    public final void stop(BundleContext context) throws Exception {
        closing = true;
        mailboxFactory.close();
    }

    @Override
    public void close() throws Exception {
        if (closing)
            return;
        Bundle bundle = bundleContext.getBundle();
        bundle.stop(Bundle.STOP_TRANSIENT);
    }
}