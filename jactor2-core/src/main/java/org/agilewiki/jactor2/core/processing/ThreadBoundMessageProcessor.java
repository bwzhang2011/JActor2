package org.agilewiki.jactor2.core.processing;

import org.agilewiki.jactor2.core.context.JAContext;
import org.agilewiki.jactor2.core.messaging.Message;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A processing bound to a pre-existing thread, a thread-bound processing can use
 * a program's main thread or a GUI thread.
 * <p>
 * For thread safety, the processing of each message is atomic, but when the processing of a
 * message results in the sending of a request, other messages may be processed before a
 * response to that request is received.
 * </p>
 * <p>
 * Request/Response messages which are destined to a different processing are buffered rather
 * than being sent immediately. These messages are disbursed to their destinations when all
 * incoming messages have been processed.
 * </p>
 * <h3>Sample Usage:</h3>
 * <pre>
 * import org.agilewiki.jactor2.core.ActorBase;
 * import org.agilewiki.jactor2.core.context.JAContext;
 * import org.agilewiki.jactor2.core.messaging.Event;
 *
 * public class ThreadBoundMailboxSample {
 *
 *     public static void main(String[] args) throws Exception {
 *
 *         //A context with no threads
 *         final JAContext jaContext = new JAContext(0);
 *
 *         //Get a reference to the main thread
 *         final Thread mainThread = Thread.currentThread();
 *
 *         //Create a thread-bound processing
 *         final ThreadBoundMessageProcessor boundMailbox =
 *             new ThreadBoundMessageProcessor(jaContext, new Runnable() {
 *                 {@literal @}Override
 *                 public void run() {
 *                     //Interrupt the main thread when there are messages to process
 *                     mainThread.interrupt();
 *                 }
 *             });
 *
 *         //Create an actor that uses the thread-bound processing
 *         final ThreadBoundActor threadBoundActor = new ThreadBoundActor(boundMailbox);
 *
 *         //Pass a FinEvent signal to the actor
 *         new FinEvent().signal(threadBoundActor);
 *
 *         //Process messages when this thread is interrupted
 *         while (true) {
 *             try {
 *                 //Wait for an interrupt
 *                 Thread.sleep(60000);
 *             } catch (InterruptedException e) {
 *                 //Process messages when the main thread is interrupted
 *                 boundMailbox.run();
 *             }
 *         }
 *     }
 * }
 *
 * class ThreadBoundActor extends ActorBase {
 *
 *     ThreadBoundActor(final MessageProcessor _mailbox) throws Exception {
 *         initialize(_mailbox);
 *     }
 *
 *     //Print "finished" and exit when fin is called
 *     void fin() throws Exception {
 *         System.out.println("finished");
 *         System.exit(0);
 *     }
 * }
 *
 * //When a FinEvent is passed to an actor, the fin method is called
 * class FinEvent extends Event&lt;ThreadBoundActor&gt; {
 *     {@literal @}Override
 *     public void processEvent(ThreadBoundActor _targetActor) throws Exception {
 *         _targetActor.fin();
 *     }
 * }
 *
 * Output:
 * finished
 * </pre>
 */
public class ThreadBoundMessageProcessor extends MessageProcessorBase {

    /**
     * The boundProcessor.run method is called when there are messages to be processed.
     */
    private final Runnable boundProcessor;

    /**
     * Create a thread-bound processing.
     * <p>
     * The _messageProcessor.run method is called when a thread-bound processing has messages
     * that need processing. As a result of invoking the run method, the
     * ThreadBoundMessageProcessor.run method needs to be invoked by the thread that
     * the processing is bound to.
     * </p>
     *
     * @param _jaContext        The context of the processing.
     * @param _boundProcessor The _messageProcessor.run method is called when there
     *                          are messages to be processed.
     */
    public ThreadBoundMessageProcessor(JAContext _jaContext, Runnable _boundProcessor) {
        super(_jaContext,
                _jaContext.getInitialBufferSize(),
                _jaContext.getInitialLocalMessageQueueSize());
        boundProcessor = _boundProcessor;
    }

    /**
     * Create a thread-bound processing.
     * <p>
     * The boundProcessor.run method is called when a thread-bound processing has messages
     * that need processing. As a result of invoking the run method, the
     * ThreadBoundMessageProcessor.run method needs to be invoked by the thread that
     * the processing is bound to.
     * </p>
     *
     * @param _jaContext             The context of the processing.
     * @param _initialOutboxSize     Initial size of the outbox for each unique message destination.
     * @param _initialLocalQueueSize The initial number of slots in the local queue.
     * @param _boundProcessor      The _messageProcessor.run method is called when there
     *                               are messages to be processed.
     */
    public ThreadBoundMessageProcessor(JAContext _jaContext,
                                       int _initialOutboxSize,
                                       final int _initialLocalQueueSize,
                                       Runnable _boundProcessor) {
        super(_jaContext, _initialOutboxSize, _initialLocalQueueSize);
        boundProcessor = _boundProcessor;
    }

    @Override
    protected void notBusy() throws Exception {
        flush();
    }

    @Override
    public final boolean isRunning() {
        return true;
    }

    @Override
    public AtomicReference<Thread> getThreadReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIdler() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Inbox createInbox(int _initialLocalQueueSize) {
        return new NonBlockingInbox(_initialLocalQueueSize);
    }

    @Override
    protected void afterAdd() throws Exception {
        boundProcessor.run();
    }

    /**
     * The flush method forwards all buffered message to their target processing for
     * processing. For results/exceptions originating from a call, the calling thread
     * is unblocked and the results returned or the exception thrown.
     * <p>
     * The flush method is automatically called when there are
     * no more messages to be processed.
     * </p>
     *
     * @return True when one or more buffered messages were delivered.
     */
    public final boolean flush() throws Exception {
        boolean result = false;
        final Iterator<Map.Entry<MessageProcessorBase, ArrayDeque<Message>>> iter = outbox.getIterator();
        if (iter != null) {
            while (iter.hasNext()) {
                result = true;
                final Map.Entry<MessageProcessorBase, ArrayDeque<Message>> entry = iter.next();
                final MessageProcessorBase target = entry.getKey();
                final ArrayDeque<Message> messages = entry.getValue();
                iter.remove();
                target.unbufferedAddMessages(messages);
            }
        }
        return result;
    }
}
