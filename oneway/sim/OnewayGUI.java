package oneway.sim;

import java.io.*;
import java.util.*;

public class OnewayGUI extends Oneway
{
    static final int PIXELS = 1280;
    static int BLOCK_WIDTH = 50;
    static int BLANK_WIDTH = 5;

    // the html of current state
    public String state() {
        String title = "Oneway";
        StringBuffer buf = new StringBuffer("");
		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\" xml:lang=\"en\">\n");
		buf.append("<head>\n");
		buf.append(" <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-7\" />\n");
		buf.append(" <title>" + title + "</title>\n");
		buf.append(" <style type=\"text/css\">\n");
		buf.append("  a:link {text-decoration: none; color: blue;}\n");
		buf.append("  a:visited {text-decoration: none; color: blue;}\n");
		buf.append("  a:hover {text-decoration: none; color: red;}\n");
		buf.append("  a:active {text-decoration: none; color: blue;}\n");
        // right arrow green
		buf.append("  .arrow-right-green {width:0;height:0;border-top:20px solid transparent;border-bottom:20px solid transparent;border-left:20px solid green;float:left;}\n");
        // right arrow red
		buf.append("  .arrow-right-red {width:0;height:0;border-top:20px solid transparent;border-bottom:20px solid transparent;border-left:20px solid red; float:left;}\n");
        // left arrow green
		buf.append("  .arrow-left-green {width:0;height:0;border-top:20px solid transparent;border-bottom:20px solid transparent;border-right:20px solid green; float:left;}\n");
        // left arrow red
		buf.append("  .arrow-left-red {width:0;height:0;border-top:20px solid transparent;border-bottom:20px solid transparent;border-right:20px solid red; float:left;}\n");
        // parking lot
        buf.append("  div.parking {width:40px;height:40px;float:left;display:inline;}");
        // empty block
        buf.append("  div.empty {width:48px;height:20px;float:left;border:1px solid black;background-color:gray}\n");
        // left bound block
        buf.append("  div.leftbound {width:50px;height:20px;float:left;display:inline}\n");
        // right bound block
        buf.append("  div.rightbound {width:50px;height:20px;float:left;display:inline}\n");
        // spaces between block
        buf.append("  div.blockspace {width:5px;height:40px;float:left;}\n");
        // cars in the parking lot
        buf.append("  div.leftcar {width:40px;height:40px;text-align:center;font-size:35px;background-color:blue}\n");
        buf.append("  div.rightcar {width:40px;height:40px;text-align:center;font-size:35px:background-color:red}\n");
        
        // parking capacity
        buf.append("  div.capacity {width:38px;height:40px;float:left;text-align:center:font-size:50;border:1px solid black;font-weight: bold;font-family: 'Comic Sans MS', cursive, sans-serif}\n");

		buf.append(" </style>\n");
		buf.append("</head>\n");
		buf.append("<body>\n");

		// general part
        buf.append(" <div style=\"width:" + PIXELS + "px; margin-left:auto; margin-right: auto;\">\n");

		// button 1
		buf.append("   <div style=\"width: 200px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;\n");
		buf.append("               font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\"><a href=\"play\">Play</a></div>\n");
		// button 2
		buf.append("   <div style=\"width: 200px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;\n");
		buf.append("               font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\"><a href=\"stop\">Stop</a></div>\n");
		// button 3
		buf.append("   <div style=\"width: 200px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;\n");
		buf.append("               font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\"><a href=\"step\">Step</a></div>\n");
        buf.append("   <div style=\"clear:both;\"></div>\n");

        // Delivered cars
		buf.append("   <div style=\"width: 500x; height: 70px; float:left; text-align: left; font-size: 25px;font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">");
        buf.append("Delivered cars:" + deliveredCars);
        buf.append("</div>\n");
        buf.append("   <div style=\"clear:both;\"></div>\n");

        // Time:
		buf.append("   <div style=\"width: 500x; height: 70px; float:left; text-align: left; font-size: 25px;font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">");
        buf.append("Time:" + tick);
        buf.append("</div>\n");
        buf.append("<div style=\"clear:both;\"></div>\n");

        printMain(buf);

        buf.append("<div style=\"clear:both;\"></div>\n");

        // show the penalty
        if (deliveredCars == cars.size()) {
            buf.append("<div style=\"width: 500px; height: 70px; float:left; text-align: left; font-size: 25px;font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">");
            buf.append(String.format("Player penalty: %.2f", penalty));
            buf.append("</div>\n");
            buf.append("<div style=\"clear:both;\"></div>\n");
        }

        // if there is something wrong
        if (errmsg != null) {
            buf.append("<div style=\"width: 500px; height: 70px; float:left; text-align: left; font-size: 25px;font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">");
            buf.append(errmsg);
            buf.append("</div>\n");
            buf.append("<div style=\"clear:both;\"></div>\n");
        }

        
		buf.append(" </div>\n");
		buf.append("</body>\n");
		buf.append("</html>\n");
		return buf.toString();
    }

    private void printMain(StringBuffer buf) {
        buf.append("<div id=\"main\"\n");
        
        for (int i = 0; i < nsegments; i++) {
            printParkingLot(buf, i);
            printRoad(buf, i);
        }
        // print the last parking lot
        printParkingLot(buf, nsegments);

        buf.append("</div>\n");
    }

    private void printRoad(StringBuffer buf, int segId) {
        // the width of the road
        int roadWidth = nblocks[segId] * BLOCK_WIDTH + (nblocks[segId]-1) * BLANK_WIDTH;
        buf.append("<div style=\"width:" + roadWidth + "; height:240px; float:left\">\n");

        // vertical space
        buf.append("<div style=\"width:" + roadWidth + "; height:120px\"></div>");
     
        // road blocks
        buf.append("<div style=\"width:" + roadWidth + "; height:45px\">");
        // initial block gap
        buf.append("<div class=\"blockspace\"></div>\n");
        
        for (int i = 0; i < nblocks[segId]; i++) {
            if (segments[segId][i] == null) {
                buf.append("<div class=\"empty\"></div>\n");
            }
            else {
                String dir = (segments[segId][i].dir > 0) ? "rightbound" : "leftbound";
                String img = (segments[segId][i].dir > 0) ? "oneway/rightcar.jpg" : "oneway/leftcar.png";

                buf.append("<div style=\"width:50px;height:80px;float:left\">");
                // the car
                buf.append("<div class=\"" + dir + "\">");
                buf.append("<img src=\"" + img + "\" width=\"50\" height=\"30\">");
                buf.append("</div>\n");
                    
                // vertical space
                buf.append("<div style=\"width:50px;height:20px;float:left;\"></div>\n");

                // the time
                buf.append("<div style=\"width:50px;height:20px;text-align:center;font-size:18px\">" + segments[segId][i].startTime + "</div>\n");
                    
                // close
                buf.append("</div>\n");
            }

            // block gap
            buf.append("<div class=\"blockspace\"></div>\n");
        }
        
        buf.append("</div>\n"); // end of road blocks
        buf.append("</div>\n"); // end of road
    }


    private void printParkingLot(StringBuffer buf, int parkId) {
        buf.append("<div style=\"width:40px;height:240px;float:left\">\n");

        // vertical space
        buf.append("<div style=\"width:40px;height:40px\"></div>\n");

        // right light
        if (parkId != nsegments) {
            String light = "arrow-right-" + (rlights[parkId] ? "green" : "red");
            buf.append("<div class=\"" + light + "\"></div>\n");
        }
        // capacity
        String cap = capacity[parkId] == Integer.MAX_VALUE ? "INF" : String.valueOf(capacity[parkId]);
        buf.append("<div class=\"capacity\">" + cap + "</div>\n");

        // right cars
        int rcars = right[parkId] == null ? 0 : right[parkId].size();
        buf.append("<div class=\"capacity\">" + rcars + "</div>\n");


        // left cars
        int lcars = left[parkId] == null ? 0 : left[parkId].size();
        buf.append("<div class=\"capacity\">" + lcars + "</div>\n");

        // left light
        if (parkId != 0) {
            String light = "arrow-left-" + (llights[parkId-1] ? "green" : "red");
            buf.append("<div class=\"" + light + "\"></div>\n");
        }
        
        buf.append("</div>"); // close parking lot
    }

    protected void play() throws Exception {
        // create a HTTP Server

		int refresh = 0;
		char req = 'X';
        HTTPServer server = new HTTPServer();
        int port = server.port();
        System.err.println("Port: " + port);
        while ((req = server.nextRequest(0)) == 'I');
        if (req != 'B')
            throw new Exception("Invalid first request");

		for (File f : directoryFiles("oneway/sim/webpages", ".html"))
			f.delete();
		FileOutputStream out = new FileOutputStream("oneway/sim/webpages/index.html");
		out.write(state().getBytes());
		out.close();

        // play the game
		for (tick = 0; tick < MAX_TICKS; ++tick) {
			boolean f = true;
			if (server != null) do {
				if (!f) refresh = 0;
				server.replyState(state(), refresh);
				while (((req = server.nextRequest(0)) == 'I') || req == 'X');
				if (req == 'S') refresh = 0;
				else if (req == 'P') refresh = 1;
				f = false;
			} while (req == 'B');
			
            // Make a copy of current status
            Parking[] lcopy = copyList(left);
            Parking[] rcopy = copyList(right);
            MovingCar[] movingcopy = movingCars.toArray(new MovingCar[0]);
            // Let the player set the lights
            player.setLights(movingcopy, lcopy, rcopy, llights, rlights);

            printLights(llights, rlights);

            boolean success = playStep(llights, rlights, tick);
            if (!success)
                break;

            printStep();

            out = new FileOutputStream("oneway/sim/webpages/" + tick + ".html");
			out.write(state().getBytes());
			out.close();

            if (deliveredCars == cars.size())
                break;
		}

        if (cars.size() != deliveredCars) {
            if (errmsg == null)
                errmsg = "Time limit exceeds.";
        }

        // clean up
		if (server != null) {
			server.replyState(state(), 0);
			while ((req = server.nextRequest(2000)) == 'I');
		}
		server.close();
    }


    public OnewayGUI(Player player, String configFilePath, String timingFilePath) {
        super(player, configFilePath, timingFilePath);
    }
}
