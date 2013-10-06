package oneway.sim;

import java.util.LinkedList;

// a dumb extension, make it easy to create
// a parking lot array
public class Parking extends LinkedList<Integer>
{
    public Parking() {
        super();
    }

    public Parking(Parking p) {
        super(p);
    }
}
