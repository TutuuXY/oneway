package oneway.dumb;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;


public class Player extends oneway.sim.Player
{
    // if the parking lot is almost full
    // it asks the opposite direction to yield
    private static double AlmostFull = 0.8;

    public Player() {}

    public void init(int nsegments, int[] nblocks, int[] capacity)
    {
        this.nsegments = nsegments;
        this.nblocks = nblocks;
        this.capacity = capacity.clone();
    }


    private boolean indicator_right = true;
    private MovingCar[] movingCars;
    private Parking[] left;
    private Parking[] right;
    private boolean[] llights;
    private boolean[] rlights;

    public void setLights(MovingCar[] movingCars,
                          Parking[] left,
                          Parking[] right,
                          boolean[] llights,
                          boolean[] rlights)
    {
        this.movingCars = movingCars;
        this.left = left;
        this.right = right;
        this.llights = llights;
        this.rlights = rlights;

        OppositeMovements();

    }

    private void OppositeMovements() 
    {
        Vector<MovingCar> v = new Vector<MovingCar>(); // to store all cars in opposite direction as indicator
        MovingCar first_one = null; // first car in same direction as indicator

        for (MovingCar car : movingCars) {
            if (indicator_right) {
                if (car.dir < 0) // if opposite direction?
                    v.add(car);
                else { // same direction as indicator, then compare to get the first car
                    if (null==first_one) first_one = car;
                    else                 first_one = faster_than(first_one, car) ? first_one:car;
                }
            } else { // same thing here, for left bound
                if (car.dir > 0)
                    v.add(car);
                else {
                    if (null==first_one) first_one = car;
                    else                 first_one = faster_than(first_one, car) ? first_one:car;
                }
            }
        }

        // take care of the cars in parking lots
        // create fake instances
        // regard the parking lots as part of the road, with the location as the nblocks th block in the corresponding segment
        if (indicator_right) {
            for (int i=0; i<left.length; i++)
                v.add( new MovingCar(i-1, nblocks, -1, 0) );
        } else {
            for (int i=0; i<right.length; i++)
                v.add( new MovingCar(i-1, nblocks, 1, 0) );
        }

        // sort the cars in opposite direction
        for (int i=0; i<v.size()-1; i++) {
            for (int j=i+1; j<v.size(); j++) {
                if ( fast_than( v.get(j), v.get(i) ) ) {
                    MovingCar temp = v.get(j);
                    v.set(j, v.get(i));
                    v.set(i, temp);
                }
            }
        }

        if (indicator_right) { // hard to write general codes for both cases. just write two versions of codes of the same functionalities
            for (MovingCar car : v) {
                // need to rewrite a lot here
            }
        } else {
            for (MovingCar car : v) {
                // need to rewrite a lot here
            }
        }



        /*
            public final int segment;
            public final int block;

            // Right bound: 1
            // Left bound: -1

            public final int dir;
            public final int startTime;

            public class Parking extends LinkedList<Integer>
        */

    }

    private boolean faster_than(MovingCar a, MovingCar b) {
        if (a.dir > 0) {
            return (a.segment > b.segment) || (a.segment==b.segment && a.block > b.block);
        } else {
            return (a.segment < b.segment) || (a.segment==b.segment && a.block < b.block);
        }
    }

    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
