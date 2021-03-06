package org.agilewiki.jactor2.core.messages;

import org.agilewiki.jactor2.core.blades.BladeBase;
import org.agilewiki.jactor2.core.facilities.Plant;
import org.agilewiki.jactor2.core.reactors.NonBlockingReactor;
import org.agilewiki.jactor2.core.reactors.Reactor;

public class RequestSample {

    public static void main(String[] args) throws Exception {

        //A facility with two threads.
        final Plant plant = new Plant(2);

        try {

            //Create blades.
            SampleBlade2 bladeA = new SampleBlade2(new NonBlockingReactor(plant));

            //Initialize blades to 1.
            bladeA.updateAReq(1).signal();

            //Change blades to 2.
            System.out.println("was " + bladeA.updateAReq(2).call() + " but is now 2");

            //Create bladeB with a reference to blades.
            IndirectBlade bladeB = new IndirectBlade(bladeA, new NonBlockingReactor(plant));

            //Indirectly change blades to 42.
            System.out.println("was " + bladeB.indirectAReq(42).call() + " but is now 42");

        } finally {
            //shutdown the facility
            plant.close();
        }

    }

}

//A simple blades with state.
class SampleBlade2 extends BladeBase {

    //Initial state is 0.
    private int state = 0;

    //Create a SimpleBlade2.
    SampleBlade2(final Reactor _reactor) throws Exception {
        initialize(_reactor);
    }

    //Return an update request.
    AsyncRequest<Integer> updateAReq(final int _newState) {
        return new AsyncBladeRequest<Integer>() {

            @Override
            protected void processAsyncRequest() throws Exception {
                int oldState = state;
                state = _newState; //assign the new state
                processAsyncResponse(oldState); //return the old state.
            }
        };
    }

}

//A blades which operates on another blades.
class IndirectBlade extends BladeBase {

    //The other blades.
    private final SampleBlade2 blade;

    //Create an IndirectBlade with a reference to another blades.
    IndirectBlade(final SampleBlade2 _bladeA, final Reactor _reactor) throws Exception {
        blade = _bladeA;
        initialize(_reactor);
    }

    //Return a request to update the other blades and return its new state.
    AsyncRequest<Integer> indirectAReq(final int _newState) {
        return new AsyncBladeRequest<Integer>() {
            AsyncRequest<Integer> dis = this;

            @Override
            protected void processAsyncRequest() throws Exception {

                //Get a request from the other blades.
                AsyncRequest<Integer> req = blade.updateAReq(_newState);

                //Send the request to the other blades.
                send(req, new AsyncResponseProcessor<Integer>() {

                    @Override
                    public void processAsyncResponse(Integer response) throws Exception {

                        //Return the old state.
                        dis.processAsyncResponse(response);
                    }
                });
            }
        };
    }
}