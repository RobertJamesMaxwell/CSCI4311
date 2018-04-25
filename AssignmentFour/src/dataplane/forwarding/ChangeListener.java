package dataplane.forwarding;

public interface ChangeListener {

    boolean changed();

}

//can possibly make ControlPlane implement ChangeListener,
//then pass in 'this' when creating DVReceiver (which extends thread)
//ie. new DVReceiver(..., ..., this)
//then, DVReceiver can update the changed boolean which would let the Control Plane
//thread know that something changed and you need to update your dv and send it out to neighbors