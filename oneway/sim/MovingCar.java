package oneway.sim;

public class MovingCar
{
    public final int segment;
    public final int block;

    // Right bound: 1
    // Left bound: -1
    public final int dir;
    public final int startTime;

    public MovingCar(int seg, int blk,
                     int d, int time)
    {
        this.segment = seg;
        this.block = blk;
        this.dir = d;
        this.startTime = time;
    }
}
