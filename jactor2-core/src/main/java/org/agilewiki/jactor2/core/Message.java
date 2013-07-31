package org.agilewiki.jactor2.core;

/**
 * <p>
 * Message encapsulates the user/application Request/Event which are queued in the Actor's mailbox. The lightweight
 * thread associated with the Actor's mailbox will process the Message asynchronously.
 * </p>
 */

abstract public interface Message extends AutoCloseable {

    /**
     * Returns true when the response is to be sent to a mailbox created from a different mailbox factory.
     *
     * @return True when the response is to be sent to a mailbox created from a different mailbox factory.
     */
    boolean isForeign();

    /**
     * Returns true when a response is expected but has not yet been placed in the message.
     *
     * @return True when a response is expected but has not yet been placed in the message.
     */
    boolean isResponsePending();

    /**
     * Execute the Event.processEvent or Request.processRequest method
     * of the event/request held by the message.
     *
     * @param _targetMailbox The mailbox whose thread is to evaluate the event/request.
     */
    void eval(final Mailbox _targetMailbox);

    /**
     * Process the throwable on the current thread in the context of the active mailbox.
     *
     * @param _activeMailbox The mailbox providing the context for processing the throwable.
     * @param _t             The throwable to be processed.
     */
    void processThrowable(final Mailbox _activeMailbox, final Throwable _t);
}