package oneway.g8;

import oneway.sim.MovingCar;
import oneway.sim.Parking;
import java.util.*;

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
    public int changeIndicatorTicks = 15;

    private boolean[] llights;
    private boolean[] rlights;
    private Parking[] right;
    private Parking[] left;
    MovingCar[] movingCars;

    public Player() {}

    public void init(int nsegments, int[] nblocks, int[] capacity)
    {
        this.nsegments = nsegments;
        this.nblocks = nblocks;
        this.capacity = capacity.clone();

        indicator = true;
        timer = 0;
    }

    //Reverse the indicator and set the lights accordingly
    public void changeIndicator(){

        indicator = !indicator;

        //Change all of the lights to point in the direction of the indicator
        for(int i = 0;i<llights.length;i++){
            //The indicator points right
            if(indicator){
                llights[i] = true;
                rlights[i] = false;
            }

            //The indicator points left
            if(!indicator){
                llights[i] = false;
                rlights[i] = true;
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
        if(timer%changeIndicatorTicks == 0){
            //changeIndicator();
        }

        for (int i=0; i<llights.length; i++) {
            llights[i] = true;
            rlights[i] = true;
        }

        OppositeMovements();

        //avoidCollissionAndOverflow();

        timer++;
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
                    if (right[i]==null) break;

                    right_parking_num += right[i].size();
                    for (int j=0; j<right[i].size(); j++) 
                        forward.add( new Car(true, i, nblocks) );
                }
            if (left!=null) 
                for (int i=0; i<left.length; i++) {
                    if (left[i]==null) break;
                    left_parking_num += left[i].size();

                    System.out.println(left[i].size()+"----------------------------------------------");
                    for (int j=0; j<left[i].size(); j++)
                        opposite.add( new Car(false, i, nblocks) );
                }
        } else {
            if (right!=null)
                for (int i=0; i<right.length; i++) {
                    if (right[i]==null) break;

                    right_parking_num += right[i].size();
                    for (int j=0; j<right[i].size(); j++)
                        opposite.add( new Car(true, i, nblocks) );
                }
            if (left!=null)
                for (int i=0; i<left.length; i++) {
                    if (left[i]==null) break;

                    System.out.println(left[i].size()+"----------------------------------------------");
                    left_parking_num += left[i].size();
                    for (int j=0; j<left[i].size(); j++)
                        forward.add( new Car(false, i, nblocks) );
                }
        }

        Collections.sort( forward );
        Collections.sort( opposite );

        if (forward.size() == 0) return ;
        int fstep = forward.get(0).steps;

        System.out.println("*************** forward cars******************");
        System.out.println("In right parking lots " + right_parking_num );
        System.out.println(forward.size() + " cars");
        for (Car c : forward)
            System.out.println(c.toString());
        System.out.println("*************** opposite cars*****************");
        if (left == null || left[left.length-1]==null)  System.out.println("No car in left round startpoint");
            else                                        System.out.println(left[left.length-1].size() + " cars in left round startpoint");

        System.out.println("In left parking lots " + left_parking_num );
        System.out.println(opposite.size() + " cars");
        for (Car c : opposite)
            System.out.println(c.toString());
        System.out.println("**********************************************");

        /*
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
        */

        /*
        if (indicator) { // hard to write general codes for both cases. just write two versions of codes of the same functionalities
            for (MovingCar car : v) {
                // need to rewrite a lot here
            }
        } else {
            for (MovingCar car : v) {
                // need to rewrite a lot here
            }
        }
        */
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

            //If the time left before the indicator change is less than the length of segment seg
            System.out.println("Time left before change:" + (changeIndicatorTicks-(timer&changeIndicatorTicks)) + "\n");
            if((changeIndicatorTicks-(timer%changeIndicatorTicks)) < nblocks[seg]){
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
