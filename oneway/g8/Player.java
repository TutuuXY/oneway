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



 


    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
