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


    public void setLights(MovingCar[] movingCars,
                          Parking[] left,
                          Parking[] right,
                          boolean[] llights,
                          boolean[] rlights)
    {


    /*
    public final int segment;
    public final int block;

    // Right bound: 1
    // Left bound: -1
    public final int dir;
    public final int startTime;
    */




        for (int i = 0; i != nsegments; ++i) {
            llights[i] = false;
            rlights[i] = false;
        }

        boolean[] indanger = new boolean[nsegments+1];
        
        // find out almost full parking lot
        for (int i = 1; i != nsegments; ++i) {
            if (left[i].size() + right[i].size() 
                > capacity[i] * AlmostFull) {
                indanger[i] = true;
            }            
        }

        for (int i = 0; i != nsegments; ++i) {
            // if right bound has car
            // and the next parking lot is not in danger
            if (right[i].size() > 0 &&
                !indanger[i+1] &&
                !hasTraffic(movingCars, i, -1)) {
                rlights[i] = true;
            }
            
            if (left[i+1].size() > 0 &&
                !indanger[i] &&
                !hasTraffic(movingCars, i, 1)) {
                llights[i] = true;
            }

            // if both left and right is on
            // find which dir is in more danger
            if (rlights[i] && llights[i]) {
                double lratio = 1.0 * (left[i+1].size() + right[i+1].size()) / capacity[i+1];
                double rratio = 1.0 * (left[i].size() + right[i].size()) / capacity[i];
                if (lratio > rratio)
                    rlights[i] = false;
                else
                    llights[i] = false;
            }
        }
    }


    // check if the segment has traffic
    private boolean hasTraffic(MovingCar[] cars, int seg, int dir) {
        for (MovingCar car : cars) {
            if (car.segment == seg && car.dir == dir)
                return true;
        }
        return false;
    }


    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
