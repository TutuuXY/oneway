package oneway.manual;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.io.*;
import java.util.*;

public class Player extends oneway.sim.Player
{
    public Player()  {}


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
        try {
            // read input from user
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please input left lights:");
            String line = reader.readLine();
            String[] fields = line.split(" ");
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].equals("t"))
                    llights[i] = true;
                else
                    llights[i] = false;
            }

            System.out.print("Please input right lights:");
            line = reader.readLine();
            fields = line.split(" ");
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].equals("t"))
                    rlights[i] = true;
                else
                    rlights[i] = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
