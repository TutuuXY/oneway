<<<<<<< HEAD
package oneway.g8;
=======
package oneway.dumb;
>>>>>>> 6ce4dd087f572f730079826421934897d3cb33f9

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;

public class Player extends oneway.sim.Player
{
    // if the parking lot is almost full
    // it asks the opposite direction to yield
    private static double AlmostFull = 0.8;
<<<<<<< HEAD
	
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
	
=======
>>>>>>> 6ce4dd087f572f730079826421934897d3cb33f9

    public Player() {}

    public void init(int nsegments, int[] nblocks, int[] capacity)
    {
        this.nsegments = nsegments;
        this.nblocks = nblocks;
        this.capacity = capacity.clone();
<<<<<<< HEAD
		
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
	
	
=======
    }

>>>>>>> 6ce4dd087f572f730079826421934897d3cb33f9

    public void setLights(MovingCar[] movingCars,
                          Parking[] left,
                          Parking[] right,
                          boolean[] llights,
                          boolean[] rlights)
    {
<<<<<<< HEAD
	
	
		this.llights = llights;
		this.rlights = rlights;
		this.movingCars = movingCars;
		this.left = left;
		this.right = right;
	

	
		//Change the indicator once changeIndicatorTicks ticks have passed
		if(timer%changeIndicatorTicks == 0){
				changeIndicator();
		}
		
		timer++;
	
		
       
    }



	
	
	//Modify the lights to avoid overflow when the indicator changes
	private void avoidOverflow(){
	
		//If the indicator points right; implement later
		for(int seg = 0;seg<nsegments;seg++){
			
			
			//If there is enough time send a car to the next parking lot, but the car will have to stop at that parking lot, make sure
			//the parking lot the car is being sent to does not overflow
			if(indicator&&seg<nsegments-1&&(timer&changeIndicatorTicks)<=(nblocks[seg]+nblocks[seg+1])){
			
				//Count of the number of cars on the segment to the right of the light
				int carsSentRight = 0;
				
				//Count of the number of cars to the right of the parking lot to the right which will reach the parking lot to the right
				//before or just as the next car is sent to the right
				int carsSentLeft = 0;
			
				//Determine the number of cars on the segment to the right and left of the parking lot to the right of the light
				for(int car = 0;car<movingCars.length;car++){
					if(movingCars[car].segment==seg)
						carsSentLeft++;
						
					if(movingCars[car].segment==seg+1)
						carsSentRight++;
				}
				
				System.out.println("Cars sent to the right for segment " + seg + ":" + carsSentRight + "\n");
				

				
				
				
				//If the number of cars moving towards the next parking lot on the right is greater than or equal to the space left in the parking lot
				if(carsSentRight+carsSentLeft>=capacity[seg+1]-(left[seg+1].size()+right[seg+1].size())){
					//Do not let any more cars pass to the next parking lot on the right
					llights[seg] = false;
					System.out.println("Switching left light off at " + seg + " to avoid overflow when the indicator switches\n");
				}
			
			}
			
			
			//If the time left before the indicator change is less than or equal to the segment length on the right side of the light
			if(indicator&&(timer%changeIndicatorTicks) <= nblocks[seg]){
			
				llights[seg] = false;

				
			}
		}
	}
=======


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
>>>>>>> 6ce4dd087f572f730079826421934897d3cb33f9


    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
