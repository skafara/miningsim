package miningsim;

import miningsim.utils.Logger;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Represents a mine and its execution logic process.
 * @author Stanislav Kafara
 * @version 1
 */
public class Mine implements Runnable {

    /** Character representing nothing to be mined. */
    public static final char MAP_VOID_CHAR = ' ';
    /** Log event format */
    public static final String LOG_FORMAT = "%d %s %d %s";

    /** Random generator used across Mine */
    public final Random RANDOM;
    /** Mine event logger */
    private final Logger LOGGER;

    /** Mine parameters */
    public final Main.InputParameters INPUT_PARAMETERS;
    /** Exception caught during executing mining process */
    private Exception THROWN_EXCEPTION;

    /** True, if map was processed, else false. */
    private boolean isMapProcessed;
    /** Represents a queue of blocks of resources to be assigned by the foreman to be mined by the workers. */
    private final Queue<Integer> resourcesBlocks;
    /** (LOCK) Used for exclusive access to the blocks of resources */
    public final Object ACCESS_TO_RESOURCES_BLOCKS;

    private final Foreman foreman;
    private final Worker[] workers;
    private final Thread[] threadsWorkers;

    /** Mine lorry */
    private Lorry lorry;
    private final Ferry ferry;
    /** Number of lorries that were prepared in the mine. */
    private int preparedLorriesCount;
    private final Collection<Thread> threadsLorries;
    /** (LOCK) Used for exclusive access to manipulating the mine lorry. */
    public final Object ACCESS_TO_LORRY;

    /** Total resources delivered to the destination counter */
    private int totalDeliveredResourcesCount;

    /**
     * Creates an instance of Mine and prepares it for the execution logic process.
     * @param inputParameters Mine parameters
     */
    public Mine(Main.InputParameters inputParameters) {
        INPUT_PARAMETERS = inputParameters;
        THROWN_EXCEPTION = null;
        RANDOM = new Random();
        LOGGER = new Logger(INPUT_PARAMETERS.o,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
        );

        foreman = new Foreman(this);
        workers = new Worker[INPUT_PARAMETERS.cWorker];
        threadsWorkers = new Thread[workers.length];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(this, i);
            threadsWorkers[i] = new Thread(workers[i]);
        }

        ACCESS_TO_RESOURCES_BLOCKS = new Object();
        resourcesBlocks = new LinkedList<>();

        preparedLorriesCount = 0;
        threadsLorries = new ArrayList<>();
        ACCESS_TO_LORRY = new Object();
        prepareLorry();
        ferry = new Ferry(this);

        isMapProcessed = false;
        totalDeliveredResourcesCount = 0;
    }

    /**
     * Represents Mine execution logic process.
     */
    @Override
    public void run() {
        try {
            // Tries to read the map and initialize the blocks of resources of the mine.
            initializeResourcesBlocks();
        } catch (IOException e) {
            THROWN_EXCEPTION = e;
            return;
        }
        // Prints mine statistics.
        printMineStatistics();
        // Starts the execution logic processes of workers.
        for (Thread threadWorker : threadsWorkers) {
            threadWorker.start();
        }
        // Waits for the workers to finish the execution of their logic processes.
        for (Thread threadWorker : threadsWorkers) {
            try {
                threadWorker.join();
            } catch (InterruptedException e) {
                //
            }
        }
        // If the mine lorry is not empty and there is nothing left to be mined,
        // depart the lorry and prepare a new one.
        synchronized (ACCESS_TO_LORRY) {
            if (!lorry.isEmpty() && !foreman.hasNextResourcesBlock()) {
                departLorry();
                prepareLorry();
            }
        }
        // Waits for all the lorries to finish the execution of their logic processes,
        // i.e. come to the destination and unload the resources.
        for (Thread threadLorry : threadsLorries) {
            try {
                threadLorry.join();
            } catch (InterruptedException e) {
                //
            }
        }
        System.out.println(Main.LOG_DIVIDER);
        // Prints final statistics of workers and delivered resources.
        printWorkersStatistics();
        printResourcesDeliveredStatistics();
        try {
            // Tries to flush the accumulated logs to the file.
            LOGGER.flush();
        }
        catch (IOException e) {
            THROWN_EXCEPTION = e;
            return;
        }
    }

    /**
     * (SIMULATION-Thread sleeps for the time of the action duration.)
     * @param duration Duration of the simulated action
     */
    public void simulateAction(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            //
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_RESOURCES_BLOCKS)
     * Returns whether there is any block of resources left to be mined.
     * @return True, if there is any block of resources left to be mined, else false.
     */
    public boolean hasNextResourcesBlock() {
        synchronized (ACCESS_TO_RESOURCES_BLOCKS) {
            return !resourcesBlocks.isEmpty();
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_RESOURCES_BLOCKS) Pops next block of resources to be mined and returns it.
     * (WARNING-This method should not be directly called by Workers.
     * Only Foreman should use this method to assign jobs to Workers.)
     * @return Next block of resources to be mined.
     * @throws NoSuchElementException If there is not any block of resources to be mined.
     */
    public int popNextResourcesBlock() throws NoSuchElementException {
        synchronized (ACCESS_TO_RESOURCES_BLOCKS) {
            return resourcesBlocks.remove();
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Departs the mine lorry.
     * - Starts a new thread of lorry execution logic process.
     * - Removes the lorry from the mine.
     */
    public void departLorry() {
        synchronized (ACCESS_TO_LORRY) {
            if (lorry == null) {
                throw new IllegalStateException("There is not any lorry in the mine.");
            }

            Thread threadLorry = new Thread(lorry);
            threadsLorries.add(threadLorry);
            threadLorry.start();
            lorry = null;
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Prepares a new mine lorry.
     * - Adds new lorry to the mine.
     */
    public void prepareLorry() {
        synchronized (ACCESS_TO_LORRY) {
            if (lorry != null) {
                throw new IllegalStateException("There is already a lorry in the mine.");
            }

            lorry = new Lorry(this, preparedLorriesCount);
            preparedLorriesCount++;
        }
    }

    /**
     * Logs an event to a console.
     * @param role Role
     * @param description Description
     */
    public void logConsoleEvent(String role, String description) {
        logConsoleEvent(role, -1, description);
    }

    /**
     * Logs an event to a console.
     * @param role Role
     * @param id Id
     * @param description Description
     */
    public void logConsoleEvent(String role, int id, String description) {
        System.out.format(LOG_FORMAT,
                System.currentTimeMillis(), role, id, description
        );
        System.out.println();
    }

    /**
     * Logs an event to a file.
     * @param role Role
     * @param description Description
     */
    public void logEvent(String role, String description) {
        logEvent(role, -1, description);
    }

    /**
     * Logs an event to a file.
     * @param role Role
     * @param id Id
     * @param description Description
     */
    public void logEvent(String role, int id, String description) {
        LOGGER.log(String.format(LOG_FORMAT,
                System.currentTimeMillis(), role, id, description
        ));
    }

    /**
     * (SYNCHRONIZED-Mine) Increases delivered resources count by provided number.
     * (INFO-Called by lorry when unloading the transported resources)
     * @param deliveredResourcesCount Number of delivered resources
     */
    public synchronized void increaseDeliveredResourcesCountBy(int deliveredResourcesCount) {
        totalDeliveredResourcesCount += deliveredResourcesCount;
    }

    /**
     * Lets foreman explore the map and identify blocks of resources.
     * Identified blocks of resources are added to the queue of blocks left to be assigned to workers and mined.
     * @throws IOException If any problem occurred during Foreman reading the map.
     */
    private void initializeResourcesBlocks() throws IOException {
        if (isMapProcessed) {
            throw new IllegalStateException("Map has already been processed.");
        }

        Collection<Integer> identifiedBlocks = foreman.processMap();
        logEvent(Foreman.class.getSimpleName(), String.format(
                "Finished analysing the input file.;blocks=%d,resources=%d",
                identifiedBlocks.size(), identifiedBlocks.stream().mapToInt(rb -> rb).sum()
        ));
        resourcesBlocks.addAll(identifiedBlocks);
        isMapProcessed = true;
    }


    /**
     * Returns the foreman.
     * @return Foreman
     */
    public Foreman getForeman() {
        return foreman;
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Returns the mine lorry.
     * @return Mine lorry
     */
    public Lorry getLorry() {
        synchronized (ACCESS_TO_LORRY) {
            return lorry;
        }
    }

    /**
     * Returns the ferry.
     * @return Ferry
     */
    public Ferry getFerry() {
        return ferry;
    }

    /**
     * Returns caught exception during executing mining process.
     * @return Caught exception if any, else null.
     */
    public Exception getThrownException() {
        return THROWN_EXCEPTION;
    }

    /**
     * Prints mine statistics.
     * - Blocks of resources count left to be mined
     * - Resources count left to be mined
     */
    private void printMineStatistics() {
        final String FORMAT = "- %-15s:\t%d%n";
        System.out.println("Mine Statistics:");
        System.out.format(FORMAT, "Blocks Count", resourcesBlocks.size());
        System.out.format(FORMAT, "Resources Count", resourcesBlocks.stream().mapToInt(rb -> rb).sum());
        System.out.println(Main.LOG_DIVIDER);
    }

    /**
     * Prints statistics of workers.
     * - Resources mined count
     */
    private void printWorkersStatistics() {
        System.out.println("Mine Workers Statistics:");
        for (Worker worker : workers) {
            System.out.format("Worker[#%d]:%n", worker.getId() + 1);
            System.out.format("- Resources Mined Count:\t%d%n", worker.getTotalResourcesMinedCount());
        }
        System.out.println(Main.LOG_DIVIDER);
    }

    /**
     * Prints statistics of delivered resources.
     * - Resources delivered count
     */
    private void printResourcesDeliveredStatistics() {
        System.out.println("Mine Resources Delivered Statistics:");
        System.out.format("- Resources Delivered Count:\t%d%n", totalDeliveredResourcesCount);
        System.out.println(Main.LOG_DIVIDER);
    }

}
