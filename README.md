One way street

=========================
Configuration file format
=========================

The configuration file follows this format:
N: number of segments
N integers: the length of each segment
N-1 integers: the capacity of each parking lot (excluding the first one and the last one)


The timing file follows this format:
A sequence of integers ordered by their absolute value.
The value of the integer is the tick the car arrives.
A positive integer is moving right. A negative integer is moving left.

=========================
Run the simulator
=========================
At your project directory (one level above oneway),
Compile:
javac oneway/sim/Oneway.java

Run:
java oneway.sim.Oneway <player name> <config file> <timing file> <gui>

For example, to run with the dumb player
java oneway.sim.Oneway dumb

Run with GUI:
Open a web browser (preferrably Firefox), enter the url:
http://localhost:port

The port number is shown after you run the program.

=========================
Road network detail
=========================

Each road segment is indexed from left to right starting from 0. So given N segments, the segments are 0, 1, 2, ... N-1. In the fo

Parking lot is indexed similarly. But since there is an parking lot both end of the roads, there are N+1 parking lots in total. So the index range of a parking lot is: 0,1,2,...N. Note that p0 and p3 has infinite capacity.

There are N traffic lights on each direction. Light[i] controls the entry into road segment i. Note that how left lights and right lights are indexed differently.


   r0      r1      r2 
   ->      ->      ->
       seg0    seg1    seg2
    | - - - | - - - | - - - |
   p0      p1       p2     p3
           <-      <-      <-
           l0       l1     l2  
   
  Fig. An illustration of a road network

