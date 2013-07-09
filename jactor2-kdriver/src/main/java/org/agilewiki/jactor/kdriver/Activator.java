package org.agilewiki.jactor.kdriver;

import org.agilewiki.jactor.api.ExceptionHandler;
import org.agilewiki.jactor.api.ResponseProcessor;
import org.agilewiki.jactor.api.Transport;
import org.agilewiki.jactor.testIface.Hello;
import org.agilewiki.jactor.osgi.FactoriesImporter;
import org.agilewiki.jactor.osgi.FactoryLocatorActivator;
import org.agilewiki.jactor.osgi.LocateService;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

public class Activator extends FactoryLocatorActivator {
    private final Logger log = LoggerFactory.getLogger(Activator.class);
    private CommandProcessor commandProcessor;

    @Override
    protected void begin(final Transport<Void> _transport) throws Exception {
        Thread.sleep(2000);
        getMailbox().setExceptionHandler(new ExceptionHandler() {
            @Override
            public void processException(Throwable throwable) throws Throwable {
                _transport.processResponse(null);
                log.error("test failure", throwable);
                getMailboxFactory().close();
            }
        });
        LocateService<CommandProcessor> locateService = new LocateService(getMailbox(),
                CommandProcessor.class.getName());
        locateService.getReq().send(getMailbox(), new ResponseProcessor<CommandProcessor>() {
            @Override
            public void processResponse(CommandProcessor response) throws Exception {
                commandProcessor = response;
                managedServiceRegistration();
                test1(_transport);
            }
        });
    }

    protected String executeCommands(final String... commands) throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        final CommandSession commandSession = commandProcessor.createSession(System.in, printStream, System.err);
        String cmds = "";
        try {
            for (String command : commands) {
                log.info(command);
                cmds = cmds + command + " || ";
                commandSession.execute(command);
            }
        } catch (Exception e) {
            log.error(cmds, e);
        }
        return byteArrayOutputStream.toString();
    }

    void test1(final Transport<Void> t) throws Exception {
        log.info(">>>>>>>>1>>>>>>>>>> " + executeCommands(
                "config:edit org.agilewiki.jactor.testService.Activator." + getVersion().toString(),
                "config:propset msg Aloha!",
                "config:propset import_a jactor2-osgi\\|" + getNiceVersion(),
                "config:update"));
        String bundleLocation = "mvn:org.agilewiki.jactor2/jactor2-test-service/" + getNiceVersion();
        FactoriesImporter factoriesImporter = new FactoriesImporter(getMailbox());
        factoriesImporter.startReq(bundleLocation).send(getMailbox(), new ResponseProcessor<Void>() {
            @Override
            public void processResponse(Void response) throws Exception {
                LocateService<Hello> locateService = new LocateService(getMailbox(), Hello.class.getName());
                locateService.getReq().send(getMailbox(), new ResponseProcessor<Hello>() {
                    @Override
                    public void processResponse(Hello response) throws Exception {
                        //log.info(">>>>>>>>>>>>>>>>>> "+executeCommands("osgi:ls", "config:list"));
                        String r = response.getMessage();
                        if (!"Aloha!".equals(r)) {
                            t.processResponse(null);
                            log.error("Unexpected response from Hello.getMessage(): " + r);
                            getMailboxFactory().close();
                            return;
                        }
                        success(t);
                    }
                });
            }
        });
    }

    void success(final Transport<Void> t) throws Exception {
        t.processResponse(null);
        Hashtable<String, String> p = new Hashtable<String, String>();
        p.put("kdriverSuccess", "true");
        bundleContext.registerService(
                KDriverSuccess.class.getName(),
                new KDriverSuccess(),
                p);
    }
}