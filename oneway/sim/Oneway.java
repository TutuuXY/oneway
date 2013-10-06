package oneway.sim;

// general utilities
import java.io.*;
import java.util.List;
import java.util.*;
import javax.tools.*;

public class Oneway
{
    static String ROOT_DIR = "oneway";

    // recompile .class file?
    static boolean recompile = true;
    
    // print more details?
    static boolean verbose = true;

    // Step by step trace
    static boolean trace = true;

    // enable gui
    static boolean gui = true;

    // default parameters
    static final String DEFAULT_PLAYER = "dumb";
    static final String DEFAULT_CONFIG = "config.txt";
    static final String DEFAULT_TIMING = "timing.txt";
    static int MAX_TICKS = 1000;
    
	// list files below a certain directory
	// can filter those having a specific extension constraint
    //
	static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

  	// compile and load players dynamically
    //
	static Player loadPlayer(String playerPath) {
        try {
            // get tools
            ClassLoader loader = Oneway.class.getClassLoader();
            if (loader == null) throw new Exception("Cannot load class loader");
            JavaCompiler compiler = null;
            StandardJavaFileManager fileManager = null;
            // get separator
            String sep = File.separator;
            // load players
            String group = playerPath;
            // search for compiled files
            File classFile = new File(ROOT_DIR + sep + group + sep + "Player.class");
            System.err.println(classFile.getAbsolutePath());
            if (!classFile.exists() || recompile) {
                // delete all class files
                List <File> classFiles = directoryFiles(ROOT_DIR + sep + group, ".class");
                System.err.print("Deleting " + classFiles.size() + " class files...   ");
                for (File file : classFiles)
                    file.delete();
                System.err.println("OK");
                if (compiler == null) compiler = ToolProvider.getSystemJavaCompiler();
                if (compiler == null) throw new Exception("Cannot load compiler");
                if (fileManager == null) fileManager = compiler.getStandardFileManager(null, null, null);
                if (fileManager == null) throw new Exception("Cannot load file manager");
                // compile all files
                List <File> javaFiles = directoryFiles(ROOT_DIR + sep + group, ".java");
                System.err.print("Compiling " + javaFiles.size() + " source files...   ");
                Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
                boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
                if (!ok) throw new Exception("Compile error");
                System.err.println("OK");
            }
            // load class
            System.err.print("Loading player class...   ");
            Class playerClass = loader.loadClass(ROOT_DIR + "." + group + ".Player");
            System.err.println("OK");
            // set name of player and append on list
            Player player = (Player) playerClass.newInstance();
            if (player == null)
                throw new Exception("Load error");
            else
                return player;

        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

	}

    void deliverCar(MovingCar car, int time)
    {
        ++deliveredCars;
        addPenalty(car.startTime, time);
        if (car.dir > 1)
            System.err.print("Right bound ");
        else
            System.err.print("Left bound ");
        System.err.println("car " + car.startTime + " is delivered");
    }

    // Play a single step of the simulation
    boolean playStep(boolean[] llights, boolean[] rlights, int time)
    {
        // create a new list of moving cars on road after this step
        LinkedList<MovingCar> newMovingCars = new LinkedList<MovingCar>();

        // add new cars in the two ending parking lots
        while (current != cars.size() &&
               Math.abs(cars.get(current)) == time) {
            System.err.println("Adding new car:" + cars.get(current));
            if (cars.get(current) < 0)
                left[nsegments].add(time);
            else
                right[0].add(time);
            current++;
        }

        // Category 1: Cars in the parking lot
        // TODO: does it cause crashing condition?
        // the distance to previous car, what if the car is in opposite direction?
        // right bound
        for (int i = 0; i != rlights.length; ++i) {
            // there is a car wait to go
            // & no other car is in the way
            if (rlights[i] &&
                right[i].size() > 0) {
                if (segments[i][0] == null) {
                    int start = right[i].removeFirst();
                    MovingCar car = new MovingCar(i, 0, 1, start);
                    segments[i][0] = car;
                    newMovingCars.add(car);
                }
                else if (segments[i][0].dir > 0) {
                    // it is a car we sent out before
                    // we stal
                }
                else {
                    // there is car coming from the opposite direction
                    // we crash
                    errmsg = String.format("Car crashs at (%d, %d)\n", i, i);
                    System.err.print(errmsg);
                    return false;
                }
            }
        }
        // left bound
        for (int i = 0; i != llights.length; ++i) {
            // NOTE:
            // get a car from parking lot i+1
            // TODO: special case when nblock = 1???
            // They may crash....
            if (llights[i] && 
                left[i+1].size() > 0) {
                if (segments[i][nblocks[i]-1] == null) {
                    int start = left[i+1].removeFirst();
                    MovingCar car = new MovingCar(i, nblocks[i]-1, -1, start);
                    segments[i][nblocks[i]-1] = car;
                    newMovingCars.add(car);
                }
                else if (segments[i][nblocks[i]-1].dir < 0) {
                }
                else {
                    errmsg = String.format("Car crashs at (%d, %d)\n", i, nblocks[i]-1);
                    System.err.print(errmsg);
                    return false;
                }
            }
        }

        // Category 2: Arriving cars
        MovingCar arriving = null;
        if ((arriving = segments[0][0]) != null &&
            arriving.dir < 0) {
            deliverCar(arriving, time);
            movingCars.remove(arriving);
            segments[0][0] = null;
        }
        if ((arriving = segments[nsegments-1][nblocks[nsegments-1]-1]) != null &&
            arriving.dir > 0) {
            deliverCar(arriving, time);
            movingCars.remove(arriving);
            segments[nsegments-1][nblocks[nsegments-1]-1] = null;
        }
        

        // Category 3: Cars that must enter a parking lot
        for (int i = 1; i != nsegments; i++) {
            if (left[i].size() != 0 || // left parking lot not empty
                llights[i-1] == false || // left light is red
                (segments[i-1][nblocks[i-1]-1] != null && segments[i-1][nblocks[i-1]-1].dir < 0))  { // not enough distance from last car
                MovingCar lcar = segments[i][0];
                if (lcar != null &&
                    lcar.dir < 0 &&
                    movingCars.contains(lcar)) {
                    left[i].add(lcar.startTime);
                    movingCars.remove(lcar);
                    segments[i][0] = null;
                    System.err.println("Left bound car " + lcar.startTime + " enters parking lot " + i);
                }
            }
            if (right[i].size() != 0 ||
                rlights[i] == false ||
                (segments[i][0] != null && segments[i][0].dir > 0)) {
                MovingCar rcar = segments[i-1][nblocks[i-1]-1];
                if (rcar != null &&
                    rcar.dir > 0 &&
                    movingCars.contains(rcar)) {
                    right[i].add(rcar.startTime);
                    movingCars.remove(rcar);
                    segments[i-1][nblocks[i-1]-1] = null;
                    System.err.println("Right bound car " + rcar.startTime + " enters parking lot " + i);
                }
            }
        }

        // Category 4: cars that may switch their positions via the parking lot
        //             or they can directly enter the next
        for (int i = 1; i != nsegments; i++) {
            // check if there is right bound car entering
            MovingCar rcar = segments[i-1][nblocks[i-1]-1];
            MovingCar lcar = segments[i][0];

            // these are actually cars that just left the parking lot
            if (rcar != null && rcar.dir < 0)
                rcar = null;
            if (lcar != null && lcar.dir > 0)
                lcar = null;

            if (rcar == null && lcar == null)
                continue;


            if (rcar == null) {
                // left car can go through
                MovingCar nlcar = new MovingCar(i-1, nblocks[i-1]-1, -1 , lcar.startTime);
                newMovingCars.add(nlcar);
                movingCars.remove(lcar);
                segments[i-1][nblocks[i-1]-1] = nlcar;
                segments[i][0] = null;
            }
            else if (lcar == null) {
                // right car can go through
                MovingCar nrcar = new MovingCar(i, 0, 1, rcar.startTime);
                newMovingCars.add(nrcar);
                movingCars.remove(rcar);
                segments[i][0] = nrcar;
                segments[i-1][nblocks[i-1]-1] = null;
            }
            else {
                // these two cars can swap positions
                MovingCar nrcar = new MovingCar(i, 0, 1, rcar.startTime);
                MovingCar nlcar = new MovingCar(i-1, nblocks[i-1]-1, -1 , lcar.startTime);
                // add to new moving car list
                newMovingCars.add(nrcar);
                newMovingCars.add(nlcar);

                // and remove them from the moving car list
                movingCars.remove(rcar);
                movingCars.remove(lcar);

                segments[i][0] = nrcar;
                segments[i-1][nblocks[i-1]-1] = nlcar;
            }
        }

        // Category 5: Intermediate cars
        boolean success = true;
        for (MovingCar car : movingCars) {
            int curseg = car.segment;
            int curblk = car.block;

            // compute its next position
            int nextblock = car.block + car.dir;
                
            //            assert (nextblock >= 0 && nextblock < nblocks);
                
            if (segments[curseg][nextblock] != null) {
                System.err.printf("Cars crash at (%d, %d)\n", curseg, nextblock);
                errmsg = String.format("Car crashs at (%d, %d)\n", curseg, nextblock);
                return false;
            }
            else {
                // car leaves current position
                segments[car.segment][car.block] = null;
                MovingCar ncar = new MovingCar(car.segment, nextblock,
                                               car.dir, car.startTime);
                segments[ncar.segment][ncar.block] = ncar;
                newMovingCars.add(ncar);
            }
        }

        // Update moving cars
        movingCars = newMovingCars;
        success = validateParking();

        if (!success) {
            errmsg = "Parking lot exceeds capacity";
        }

        return success;
    }

    // // Play a single step of the simulation
    // boolean playStep(boolean[] llights, boolean[] rlights, int time)
    // {
    //     // create a new list of moving cars on road after this step
    //     LinkedList<MovingCar> newMovingCars = new LinkedList<MovingCar>();

    //     // add new cars in the two ending parking lots
    //     while (current != cars.size() &&
    //            Math.abs(cars.get(current)) == time) {
    //         System.err.println("Adding new car:" + cars.get(current));
    //         if (cars.get(current) < 0)
    //             left[nsegments].add(time);
    //         else
    //             right[0].add(time);
    //         current++;
    //     }

    //     // check each light to see if a car can go through
    //     //        
    //     // right bound
    //     for (int i = 0; i != rlights.length; ++i) {
    //         // there is a car wait to go
    //         // & no other car is in the way
    //         if (rlights[i] &&
    //             right[i].size() > 0 &&
    //             segments[i][0] == null) {
    //             int start = right[i].removeFirst();
    //             MovingCar car = new MovingCar(i, 0, 1, start);
    //             segments[i][0] = car;
    //             newMovingCars.add(car);
    //         }
    //     }
    //     // left bound
    //     for (int i = 0; i != llights.length; ++i) {
    //         // NOTE:
    //         // get a car from parking lot i+1
    //         // TODO: special case when nblock = 1???
    //         // They may crash....
    //         if (llights[i] && 
    //             left[i+1].size() > 0 &&
    //             segments[i][nblocks-1] == null) {
    //             int start = left[i+1].removeFirst();
    //             MovingCar car = new MovingCar(i, nblocks-1, -1, start);
    //             segments[i][nblocks-1] = car;
    //             newMovingCars.add(car);
    //         }
    //     }

    //     // special case handling when two
    //     // cars are entering an empty parking lot
    //     // For each empty parking lot
    //     // check if can swap two cars
    //     for (int i = 1; i != nsegments; i++) {
    //         if (left[i].size() != 0 || right[i].size() != 0) 
    //             continue;

    //         // check if there is right bound car entering
    //         MovingCar rcar = segments[i-1][nblocks-1];
    //         if (rcar == null || 
    //             rcar.dir < 0 ||
    //             !movingCars.contains(rcar))
    //             continue;

    //         // check if there is left bound car entering
    //         MovingCar lcar = segments[i][0];
    //         if (lcar == null || 
    //             lcar.dir > 0 ||
    //             !movingCars.contains(lcar))
    //             continue;

    //         // now we can swap these two cars
    //         MovingCar nrcar = new MovingCar(i, 0, 1, rcar.startTime);
    //         MovingCar nlcar = new MovingCar(i-1, nblocks-1, -1 , lcar.startTime);
    //         // add to new moving car list
    //         newMovingCars.add(nrcar);
    //         newMovingCars.add(nlcar);

    //         // and remove them from the moving car list
    //         movingCars.remove(rcar);
    //         movingCars.remove(lcar);
    //     }
        

    //     // mark if a car encounter at crossroad (parking lot) i
    //     boolean[] encountered = new boolean[nsegments+1];
    //     boolean success = true;

    //     // Check each moving car in last round
    //     for (MovingCar car : movingCars) {
    //         int curseg = car.segment;
    //         int curblk = car.block;
            
    //         // left bound entering a crossroad
    //         if (curblk == 0 && car.dir < 0) {
    //             int nextseg = car.segment - 1;
    //             // car arrived
    //             if (nextseg < 0)
    //                 deliverCar(car, time);
    //             else {
    //                 // entering the parking lot or move ahead?
    //                 // 1. the traffic is green
    //                 // 2. no car is in the parking lot
    //                 // 3. no car is on the way, this also includes that
    //                 //    a new car just left the parking
    //                 // 4. no opposite direction car is entering the crossroad

    //                 if (llights[nextseg] &&
    //                     left[nextseg+1].size() == 0 && 
    //                     segments[nextseg][nblocks-1] == null &&
    //                     !encountered[curseg]) {
    //                     // move ahead
    //                     MovingCar ncar = new MovingCar(nextseg, nblocks-1, -1, car.startTime);
    //                     segments[car.segment][car.block] = ncar;
    //                     newMovingCars.add(ncar);
    //                 }
    //                 else
    //                     // enter the parking lot
    //                     left[nextseg+1].add(car.startTime);
    //             }

    //             // a car enters the crossroad
    //             encountered[curseg] = true;
    //             // this car must move
    //             segments[curseg][curblk] = null;
    //         }
    //         else if (car.block == nblocks-1 && car.dir > 0) {
    //             int nextseg = car.segment + 1;
    //             if (nextseg == nsegments)
    //                 deliverCar(car, time);
    //             else {
    //                 if (rlights[nextseg] &&
    //                     right[nextseg].size() == 0 &&
    //                     segments[nextseg][0] == null &&
    //                     !encountered[nextseg]) {
    //                     MovingCar ncar = new MovingCar(nextseg, 0, 1, car.startTime);
    //                     segments[nextseg][0] = ncar;
    //                     newMovingCars.add(ncar);
    //                 }
    //                 else
    //                     right[nextseg].add(car.startTime);
    //             }

    //             // a car enters the crossroad
    //             encountered[nextseg] = true;
    //             // this car must move
    //             segments[curseg][curblk] = null;

    //         }
    //         else {
    //             // compute its next position
    //             int nextblock = car.block + car.dir;
                
    //             assert nextblock >= 0 && nextblock < nblocks;
                
    //             if (segments[curseg][nextblock] != null) {
    //                 System.err.printf("Cars crash at (%d, %d)\n", curseg, nextblock);
    //                 success = false;
    //                 break;
    //             }
    //             else {
    //                 // car leaves current position
    //                 segments[car.segment][car.block] = null;
    //                 MovingCar ncar = new MovingCar(car.segment, nextblock,
    //                                                car.dir, car.startTime);
    //                 segments[ncar.segment][ncar.block] = ncar;
    //                 newMovingCars.add(ncar);
    //             }
    //         }
            
    //     }

    //     if (success) {
    //         // Update moving cars
    //         movingCars = newMovingCars;
    //         success = validateParking();
    //     }
    //     else
    //         movingCars = null;

    //     return success;
    // }



    boolean validateParking()
    {
        for (int i = 1; i != nsegments; ++i) {
            if (left[i].size() + right[i].size() > capacity[i]) {
                System.err.printf("Parking lot %d exceeds its capacity %d\n", i, capacity[i]);
                return false;
            }
        }
        return true;
    }

    protected void play() throws Exception {

        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        tick = 0;

        printConfig();
        printStep();

        for (tick = 1; tick <= MAX_TICKS; tick++) {
            // step by step trace
            if (trace) {
                try {
                    System.err.print("$");
                    buffer.readLine();
                } catch (Exception e) {}
            }

            // Make a copy of current status
            Parking[] lcopy = copyList(left);
            Parking[] rcopy = copyList(right);
            MovingCar[] movingcopy = movingCars.toArray(new MovingCar[0]);
            // Let the player set the lights
            player.setLights(movingcopy, lcopy, rcopy, llights, rlights);
            
            printLights(llights, rlights);

            // simulate one step
            boolean success = playStep(llights, rlights, tick);
            // does simulation succeed?
            if (!success)
                break;

            printStep();

            // does game end?
            if (deliveredCars == cars.size())
                break;
        }

        if (cars.size() != deliveredCars)
            System.err.println("Player fails to deliver all the cars.");
        else
            System.err.println("Player finishes with penalty: " + penalty);
    }


    void addPenalty(int start, int time) {
        double L = time - start;
        penalty += ( L * Math.log(L) - mintime* Math.log(mintime) );
    }


    void readConfig(String configFilePath) {
        try {
            Scanner s = null;
            s = new Scanner(new BufferedReader(new FileReader(configFilePath)));
            
            mintime = 0;

            // nsegments
            nsegments = s.nextInt();

            nblocks = new int[nsegments];

            // nblocks
            for (int i = 0; i < nsegments; i++) {
                nblocks[i] = s.nextInt();
                mintime = mintime + nblocks[i];
            }

            capacity = new int[nsegments+1];
            capacity[0] = capacity[nsegments] = Integer.MAX_VALUE;

            // capacities
            for (int i = 1; i != nsegments; ++i)
                capacity[i] = s.nextInt();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }        
        
        // segment+1 parking lots
        left = new Parking[nsegments+1];
        right = new Parking[nsegments+1];

        for (int i = 0; i != nsegments+1; ++i) {
            if (i != 0)
                left[i] = new Parking();

            if (i != nsegments)
                right[i] = new Parking();
        }
            
        // initialize the road
        segments = new MovingCar[nsegments][];

        for (int i = 0; i < nsegments; i++)
            segments[i] = new MovingCar[nblocks[i]];

        // light i controls cars that enter segment i
        llights = new boolean[nsegments];
        rlights = new boolean[nsegments];

        // before the game starts
        // set moving cars to empty
        movingCars = new LinkedList<MovingCar>();

        // init the player
        player.init(nsegments, nblocks, capacity.clone());
    }


    // read the timing into a timing file
    void readTiming(String timingPath) {
        try {
            Scanner scanner;
            scanner = new Scanner(new BufferedReader(new FileReader(timingPath)));
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line == "")
                    continue;
                cars.add(Integer.parseInt(line));
                    
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Oneway(Player player, String configFilePath, String timingFilePath)
    {
        // set the player
        this.player = player;

        // read the config
        readConfig(configFilePath);

        // read the timing file
        readTiming(timingFilePath);
    }

    
    // Oneway <playername> <configfile> <timingfile>
	public static void main(String[] args) throws Exception
	{
        String playerPath, configPath, timingPath;

        String sep = File.separator;

        // players path
        if (args.length > 0)
            playerPath = args[0];
        else
            playerPath = DEFAULT_PLAYER;

        // config
        if (args.length > 1)
            configPath = args[1];
        else
            configPath = DEFAULT_CONFIG;

        // timing
        if (args.length > 2)
            timingPath = args[2];
        else
            timingPath = DEFAULT_TIMING;

        // gui
        if (args.length > 3)
            gui = Boolean.parseBoolean(args[3]);

        // recompile
        if (args.length > 4)
            recompile = Boolean.parseBoolean(args[4]);

        // verbose
        if (args.length > 5)
            verbose = Boolean.parseBoolean(args[5]);
        
        // trace
        if (args.length > 6)
            trace = Boolean.parseBoolean(args[6]);

        // load all the players
        Player player = loadPlayer(playerPath);

        Oneway game;
        if (gui)
            game = new OnewayGUI(player, configPath, timingPath);
        else
            game = new Oneway(player, configPath, timingPath);
        game.play();
    }        

	static int[] copyI(int[] a)
	{
		if (a == null) return null;
		int[] b = new int [a.length];
		for (int i = 0 ; i != a.length ; ++i)
			b[i] = a[i];
		return b;
	}

	static int[][] copyII(int[][] a)
	{
		int[][] b = new int [a.length][];
		for (int i = 0 ; i != a.length ; ++i)
			b[i] = copyI(a[i]);
		return b;
	}

    static Parking[] copyList(Parking[] l) {
        if (l == null)
            return null;
        Parking[] copy = new Parking[l.length];
        for (int i = 0; i != l.length; ++i)
            if (l[i] != null)
                copy[i] = new Parking(l[i]);
        return copy;
    }

    void printConfig()
    {
        System.err.println("##### Configuration #####");
        System.err.println("Number of segments: " + nsegments);
        System.err.println("Number of blocks per segment: " + nblocks);
        System.err.print("Parking lot capacity: ");
        for (int i = 0; i <= nsegments; ++i) {
            if (i == 0 || i == nsegments)
                System.err.print("inf");
            else
                System.err.print(capacity[i]);
            System.err.print(" ");
        }
        System.err.println();
    }

    // print the lights
    // <=, =>, <=>, =
    void printLights(boolean[] llights, boolean[] rlights)
    {
        System.err.println("##### Setting traffic lights #####");

        for (int i = 0; i != nsegments + 1; ++i) {
            if (i != 0 && llights[i-1] == true)
                System.err.print("<");
            else
                System.err.print(" ");
            System.err.print("=");
            if (i != nsegments && rlights[i] == true) 
                System.err.print(">");
            else
                System.err.print(" ");
            if (i != nsegments) {
                int nspace = 4 * nblocks[i] - 1;
                printSpaces(nspace);
            }
        }
        System.err.println();
    }

    // digits of a number
    static int spaces(int num)
    {
		int spaces = 0;
		if (num <= 0) {
			spaces = 1;
			num = -num;
		}
		while (num != 0) {
			spaces++;
			num /= 10;
		}
		return spaces;
    }

    void printSpaces(int nspace)
    {
        System.err.print(String.format("%" + nspace + "s", " "));
    }

    // print current step
    // 7->           5->   
    //  | -- -> -- -> | -- -- -- -- |
    //               4<-  
    void printStep()
    {
        System.err.println("##### Tick " + tick + " #####");

        // print right bound parking lot

        for (int i = 0; i != nsegments; ++i) {
            System.err.printf("%d->", right[i].size());
            int nspace = 4 * nblocks[i] - 1;
            printSpaces(nspace - spaces(right[i].size()) + 1);
        }
        System.err.println();

        // print traffic
        for (int seg = 0; seg != nsegments; ++seg) {
            System.err.print(" |");
            
            for (int block = 0; block != nblocks[seg]; ++block) {
                System.err.print(" ");
                if (segments[seg][block] == null)
                    System.err.print("---");
                else if (segments[seg][block].dir > 0)
                    System.err.printf("%2d>", segments[seg][block].startTime);
                else if (segments[seg][block].dir < 0)
                    System.err.printf("<%-2d", segments[seg][block].startTime);
            }
        }
        System.err.print(" |");
        System.err.println();

        // print left bound parking lot
        printSpaces(4 * nblocks[0] - 1 + 3);
        for (int i = 1; i != nsegments + 1; ++i) {
            System.err.printf("%d<-", left[i].size());

            if (i != nsegments) {
                int nspace = 4 * nblocks[i] - 1;
                printSpaces(nspace - spaces(left[i].size()) + 1);
            }
        }
        System.err.println();
    }

    // player class
    Player player;

    // game configuration
    int nsegments;
    int[] nblocks;
    //    int nblocks;

    // car timing sequence
    ArrayList<Integer> cars = new ArrayList<Integer>();
    // current position in sequence
    int current;

    // All cars on road
    LinkedList<MovingCar> movingCars;

    // park cars heading left
    // parking lot is indexed from the leftmost to the rightmost
    // index range [0 - nsegments]
    Parking[] left;
    // park cars heading right
    Parking[] right;
    // capacity of the parking lot
    int[] capacity;
    // the status of each road segment
    MovingCar[][] segments;

    // traffic light
    // light[i] controls a car entering the ith segment
    // index range [0-nsegments)
    boolean[] llights;
    boolean[] rlights;
    
    int tick;

    // number of cars delivered
    int deliveredCars;
    // latency score
    double penalty;
    int mintime;

    // error message for GUI display
    String errmsg;
}
