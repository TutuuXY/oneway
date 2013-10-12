package oneway.g8;

import oneway.sim.MovingCar;

public class Car implements Comparable<Car> {
    public int steps;
    public int d;
    public int seg;
    public int blk;
    public int time;
    public int latest_parking;
    public int actual_parking;
	public int ID = -1; //Unique car identifier

    public Car(int seg, int blk,
                     int d, int time, int[] nblocks) {
        this.seg = seg;
        this.blk = blk;
        this.time = time;
        this.d = d;
        int blks = 0;
        for (int i=0; i<seg; i++)
            blks += nblocks[i];
        this.steps = blks + this.blk;
    }
	
	
	//Modified step mechanism
	public Car(int seg, int blk,
                int d, int time, int[] nblocks, boolean indicator, int ID) {
        this.seg = seg;
        this.blk = blk;
        this.time = time;
        this.d = d;
        int blks = 0;
        for (int i=0; i<seg; i++)
            blks += nblocks[i];
			
		if(indicator)
			this.steps = blks + 1 + this.blk;
		else
			this.steps = blks + this.blk;
		this.ID = ID;
    }

    public Car( boolean in_right_Parking, int ind, int[] nblocks ) {
        this.seg = ind;
        this.blk = -1;
        this.d = (in_right_Parking) ? 1:-1;
        this.time = 0;
        int blks = 0;
        for (int i=0; i<seg; i++)
            blks += nblocks[i];
        this.steps = blks + 1 + this.blk;
    }
	
	
	//Modified step mechanism
    public Car( boolean in_right_Parking, int ind, int[] nblocks, boolean indicator, int ID ) {
        this.seg = ind;
		if(indicator)
			this.blk = -1;
		else
			this.blk = 0;
        this.d = (in_right_Parking) ? 1:-1;
        this.time = 0;
        int blks = 0;
        for (int i=0; i<seg; i++)
            blks += nblocks[i];
		if(indicator)
			this.steps = blks + 1 + this.blk;
		else
			this.steps = blks + this.blk;
		this.ID = ID;
    }
	
    
    public int compareTo(Car c) {
        if (this.d > 0) {
            return c.steps - this.steps;
        } else {
            return this.steps - c.steps;
        }
    }

    public String toString() {
        return "seg=" + seg + " blk=" + blk + " dir=" + d + " stime=" + time + " steps=" + steps + " latest_parking=" + latest_parking + " actual_parking=" + actual_parking + " ID=" + ID;
    }
}
