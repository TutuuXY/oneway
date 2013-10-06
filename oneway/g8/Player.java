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
	public boolean indicator;
	
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

        OppositeMovements();
		avoidCollissionAndOverflow();

		//Change the indicator once changeIndicatorTicks ticks have passed
		if(timer%changeIndicatorTicks == 0){
				changeIndicator();
		}
		
		timer++;
    }

    private void OppositeMovements() 
    {
        Vector<MovingCar> v = new Vector<MovingCar>(); // to store all cars in opposite direction as indicator
        MovingCar first_one = null; // first car in same direction as indicator

        for (MovingCar car : movingCars) {
            if (indicator) {
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
        if (indicator) {
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

        if (indicator) { // hard to write general codes for both cases. just write two versions of codes of the same functionalities
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
