package org.agilewiki.jactor3;

import java.util.concurrent.Semaphore;

abstract public class SignalImpl<TARGET extends Actor>
        extends MessageImpl<TARGET> implements Signal<TARGET> {

    /**
     * Create a Signal message.
     *
     * @param _message     The message that provides the context.
     * @param _targetActor The target actor.
     */
    public SignalImpl(final Message _message, final TARGET _targetActor) {
        super(_message, _targetActor);
    }

    @Override
    public Message iteration() {
        Semaphore sourceSemaphore = getSourceSemaphore();
        TARGET targetActor = getTargetActor();
        Semaphore targetSemaphore = getTargetActor().getSemaphore();
        boolean sameSemaphore = sourceSemaphore == targetSemaphore;
        if (sameSemaphore) {
            if (!isSameThread()) {
                try {
                    targetSemaphore.acquire();
                } catch (InterruptedException e) {
                    return null;
                }
            }
        } else {
            try {
                targetSemaphore.acquire();
            } catch (InterruptedException e) {
                return null;
            }
            if (isSameThread()) {
                sourceSemaphore.release();
            }
        }
        Message message = null;
        try {
            message = processSignal(targetActor);
        } catch (Throwable e1) {
            ExceptionHandler exceptionHandler = getExceptionHandler();
            if (exceptionHandler == null) {
                targetSemaphore.release();
                e1.printStackTrace();
            } else
                try {
                    exceptionHandler.processException(e1);
                } catch (Throwable e2) {
                    targetSemaphore.release();
                    e2.printStackTrace();
                }
        }
        if (message == null) {
            targetActor.getSemaphore().release();
            return null;
        }
        return message;
    }
}