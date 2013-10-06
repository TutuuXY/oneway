package oneway.sim;

import java.util.*;

public abstract class Player
{
    // initialize the player
    // capacity: the capacity of each parking lot
    // the first and last parking lot has capacity Integer.MAX_VALUE
    public abstract void init(int nsegments, int[] nblocks, int[] capacity);


    // set the lights
    // Input:
    // movingCars - an array of all cars that are on the road now, i.e. cars that are on a road segment. Cars in the parking lot are not in this list
    // left - The queue of leftbound cars in every parking lot
    // right - The queue of rightbound cars in every parking lot
    //        Parking is just a linkedlist
    // Output:
    // llights, rlights - You should set your lights into these two arrays
    //   light[i] is the light that controls the entry to road segment i
    public abstract void setLights(MovingCar[] movingCars,
                                   Parking[] left,
                                   Parking[] right,
                                   boolean[] llights, boolean[] rlights);
}
