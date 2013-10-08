package oneway.g8;

import oneway.sim.MovingCar;
import oneway.sim.Parking;
import java.util.*;
import java.lang.*;

public class Player extends oneway.sim.Player
{
    // if the parking lot is almost full
    // it asks the opposite direction to yield
    private static double AlmostFull = 0.8;

    //Indicator points right if true and left if not true.
    public boolean indicator = false;

    //Number of ticks since the game began
    public int timer;

    //Number of ticks until the indicator should change
    public int changeIndicatorTicks = 30;

    private boolean[] llights;
    private boolean[] rlights;
    private Parking[] right;
    private Parking[] left;
    MovingCar[] movingCars;
    private int nblocks_sum;

    public Player() {}

    public void init(int nsegments, int[] nblocks, int[] capacity)
    {
        this.nsegments = nsegments;
        this.nblocks = nblocks;
        this.capacity = capacity.clone();

        nblocks_sum = 0;
        for (int i=0; i<nblocks.length; i++)
            nblocks_sum += nblocks[i];

        indicator = false;
        timer = 0;
    }


    //Determine if all road segments are the same
    public boolean sameLength(){
        for(int i= 1;i<nblocks.length;i++){
            if(nblocks[i-1]!=nblocks[i])
                return false;
        }

        return true;
    }

    //Reverse the indicator and set the lights accordingly
    public void setLights(){


        //Change all of the lights to point in the direction of the indicator
        for(int i = 0;i<llights.length;i++){
            //The indicator points right
            if(indicator){
                llights[i] = false;
                rlights[i] = true;
            }

            //The indicator points left
            if (!indicator){
                llights[i] = true;
                rlights[i] = false;
            }
        }
    }

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

        //Change the indicator once changeIndicatorTicks ticks have passed
        /*
        if (timer % changeIndicatorTicks == 0){
            indicator = !indicator;
        }
        */

        setLights();

        OppositeMovements();

        print_lights();

        //avoidCollissionAndOverflow();

        timer++;
    }

    private void print_lights() {
        for (int i=0; i<rlights.length; i++)
            System.out.print(((rlights[i])?0:1) + " ");
        System.out.print("\n  ");
        for (int i=0; i<llights.length; i++)
            System.out.print(((llights[i])?0:1) + " ");
        System.out.println("");
    }

    private void OppositeMovements() 
    {
        Vector<Car> opposite = new Vector<Car>( ); // to store all cars in opposite direction as indicator
        Vector<Car> forward  = new Vector<Car>( );

        // segment, block, dir, startTime

        for (MovingCar c : movingCars) {
            if (indicator) {
                if (c.dir < 0)  opposite.add(new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                else            forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks));

            } else { 
                if (c.dir < 0)  forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                else            opposite.add(new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
            }
        }

        int left_parking_num = 0;
        int right_parking_num = 0;

        // take care of the cars in parking lots
        if (indicator) {
            if (right!=null)
                for (int i=0; i<right.length; i++) {
                    if (right[i]==null) continue;

                    right_parking_num += right[i].size();
                    for (int j=0; j<right[i].size(); j++) 
                        forward.add( new Car(true, i, nblocks) );
                }
            if (left!=null)
                for (int i=0; i<left.length; i++) {
                    if (left[i]==null) continue;
                    left_parking_num += left[i].size();

                    for (int j=0; j<left[i].size(); j++)
                        opposite.add( new Car(false, i, nblocks) );
                }
        } else {
            if (right!=null)
                for (int i=0; i<right.length; i++) {
                    if (right[i]==null) continue;

                    right_parking_num += right[i].size();
                    for (int j=0; j<right[i].size(); j++) {
                        opposite.add( new Car(true, i, nblocks) );
                        opposite.get( opposite.size()-1 ).steps--;
                    }
                }
            if (left!=null)
                for (int i=0; i<left.length; i++) {
                    if (left[i]==null) continue;

                    left_parking_num += left[i].size();
                    for (int j=0; j<left[i].size(); j++)
                        forward.add( new Car(false, i, nblocks) );
                }
        }

        Collections.sort( forward );
        Collections.sort( opposite );

        if (forward.size() == 0 || opposite.size() == 0) { // if only have cars in one direction, set all lights green
            for (int i=0; i<llights.length; i++) {
                llights[i] = true;
                rlights[i] = true;
            }
            return ; 
        }

        int fstep = forward.get(0).steps; // get the first car in the forward direction
        oneway.sim.Parking[] L = left.clone();
        oneway.sim.Parking[] R = right.clone();

        for (int i=0; i<L.length; i++)
            if (L[i] == null)
                L[i] = new oneway.sim.Parking();
        for (int i=0; i<R.length; i++)
            if (R[i] == null)
                R[i] = new oneway.sim.Parking();

        if (indicator) {
            for (int i=llights.length/2; i<llights.length; i++)
                llights[i] = true;
            for (Car c : opposite) {
                int cstep = (fstep + c.steps) / 2; // collision steps
                int cseg = get_seg(cstep); // collision segment,FIXME right now all segments are regarded as same length
                int latest_parking = cseg + 1;
                c.latest_parking = latest_parking;

                if (c.steps <= fstep) { // first forward car already passed and the current car is in parking lot
                    int seg = get_seg(c.steps);
                    if (seg > 0)
                        llights[seg - 1] = false;
                    continue;
                }

                for (int i=latest_parking; i<L.length; i++) { // starting from the latest_parking to where the car come from

                    if (R[i].size() + L[i].size() < capacity[i]) {
                        L[i].add(0);

                        int car_seg = get_seg(c.steps);
                        int car_blk = get_blk(c.steps);
                        if (car_seg == i && car_blk == 0)
                            llights[i-1] = false;
                        break;
                    }
                }
            }

            // control the left direction startpoint
            int cstep = (fstep + nblocks_sum + 1) / 2;
            int cseg = get_seg(cstep);
            if (cseg == nsegments-1) {
                llights[llights.length-1] = false;
            }
        } else {
            for (int i=0; i<rlights.length/2; i++)
                rlights[i] = true;
            for (Car c : opposite) {
                int cstep = (fstep + c.steps + 1) / 2; // collision steps
                int cseg = get_seg(cstep); // collision segment
                int latest_parking = cseg;
                c.latest_parking = latest_parking;

                if (c.steps >= fstep) { // first forward car already passed and the current car is in parking lot
                    int seg = get_seg(c.steps);
                    if (seg < rlights.length-1)
                        rlights[seg+1] = false;
                    continue;
                }

                for (int i=latest_parking; i>=0; i--) {
                    if (R[i].size() + L[i].size() < capacity[i]) {
                        R[i].add(0);

                        int car_seg = get_seg(c.steps);
                        int car_blk = get_blk(c.steps);

                        if (car_seg == i-1 && car_blk == nblocks[i-1]-1) //FIXME index
                            rlights[i] = false;
                        break;
                    }
                }
            }

            // control the right direction startpoint
            int cstep = fstep / 2;
            int cseg = get_seg(cstep);
            if (cseg == 0) {
                rlights[0] = false;
            }
        }

        /*
           r0      r1      r2 
           ->      ->      ->
           seg0    seg1    seg2
           | - - - | - - - | - - - |
           p0      p1       p2     p3
                   <-      <-      <-
                   l0       l1     l2  
         */

        // take care of the following situation:
        // opposite cars shouldn't wait until forward cars arrive at destination, which is the endpoint of the road
        // commented out for now, becuase not sure whether there will be new forward cars appearing in the endpoint of road
        /*
        if (indicator) {
            for (int i=0; i<llights.length; i++)
                if (forward.get(forward.size()-1).seg >= i+1)
                    llights[i] = true;
        } else {
            for (int i=0; i<rlights.length; i++)
                if (forward.get(forward.size()-1).seg < i)
                    rlights[i] = true;
        }
        */

        System.out.println("*************** forward cars******************");
        System.out.println(forward.size() + " cars moving forward");
        for (Car c : forward)
            System.out.println(c.toString());
        System.out.println("*************** opposite cars*****************");
        System.out.println(opposite.size() + " cars moving opposite");
        for (Car c : opposite)
            System.out.println(c.toString());
        System.out.println("**********************************************");

    }

    // get the segment number according to steps of a car
    int get_seg(int steps) {
        steps++;
        int sum = 0;
        for (int i=0; i<nblocks.length; i++) {
            sum += nblocks[i];
            if (sum >= steps)
                return i; // guaranteed to execute this return
        }
        return nblocks.length;
    }

    // get the block number in a segment according to steps of a car
    int get_blk(int steps) {
        steps++;
        int sum = 0;
        for (int i=0; i<nblocks.length; i++) {
            sum += nblocks[i];
            if (sum >= steps) {
                sum -= nblocks[i];
                return steps - sum - 1; // guaranteed to execute this return
            }
        }
        return 0;
    }

    //Modify the lights to avoid overflow when the indicator changes
    private void avoidCollissionAndOverflow(){

        for(int seg = 0;seg<nsegments;seg++){

            //Ensure parking lot p_(seg+1) does not overflow 
            //If there is not enough time for a car to pass the parking lot betewen seg and seg+1
            if(seg<nsegments-1&&(changeIndicatorTicks-(timer&changeIndicatorTicks)) <= nblocks[seg]+nblocks[seg+1]){
                //Cars moving to parking lot p_(seg+1) from the right
                int carsRight = 0;

                //Cars moving to parking lot p_(seg+1) from the left
                int carsLeft = 0;

                for(int car = 0;car<movingCars.length;car++){
                    if(movingCars[car].segment==seg&&movingCars[car].dir==1)
                        carsRight++;

                    if(movingCars[car].segment==seg+1&&movingCars[car].dir==-1)
                        carsLeft++;
                }

                //If there is not enough room to accomodate more cars at parking lost seg+1, do not send more cars to that parking lot
                if(carsRight+carsLeft>=capacity[seg+1]-left[seg+1].size()-right[seg+1].size()){
                    if(indicator)
                        rlights[seg] = false;
                    if(!indicator)
                        llights[seg+1]= false;
                }
            }

            //Ensure parking lot p_(seg+1) does not overflow while indicator points left
            //If there is not enough time for a car to pass the parking lot betewen seg and seg+1
            if(indicator&&seg<nsegments-1&&(changeIndicatorTicks-(timer&changeIndicatorTicks)) <= nblocks[seg]+nblocks[seg+1]){

                //Cars moving to parking lot p_(seg+1) from the right
                int carsRight = 0;

                //Cars moving to parking lot p_(seg+1) from the left
                int carsLeft = 0;

                for(int car = 0;car<movingCars.length;car++){
                    if(movingCars[car].segment==seg)
                        carsRight++;

                    if(movingCars[car].segment==seg+1)
                        carsLeft++;
                }
                //If there is not enough room to accomodate more cars at parking lost seg+1, do not send more cars to that parking lot
                if(carsRight+carsLeft>=capacity[seg+1]-left[seg+1].size()-right[seg+1].size())
                    rlights[seg] =false;
            }

            //If the time left before the indicator change is less than or equal to	 the length of segment seg
            System.out.println("Time left before change:" + (changeIndicatorTicks-(timer&changeIndicatorTicks)) + "\n");
            if((changeIndicatorTicks-(timer%changeIndicatorTicks)) <= nblocks[seg]){
                if(indicator)
                    rlights[seg] = false;

                if(!indicator)
                    llights[seg] = false;
            }
        }
    }

    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
