package miningsim;

import miningsim.utils.IRandomGeneration;

/**
 * Represents a lorry and its execution logic process.
 * @author Stanislav Kafara
 * @version 1
 */
public class Lorry implements Runnable, IRandomGeneration<Integer> {

    /** Mine the Lorry is associated with */
    private final Mine mine;
    /** Lorry ID */
    private final int ID;

    /** Timestamp of the lorry creation */
    private final long TIMESTAMP_IS_CREATED;
    /** Timestamp of the lorry being fully loaded */
    private long timestampIsFull;

    /** Number of resources loaded onto the lorry */
    private int loadedResourcesCount;

    /**
     * Creates a lorry transporting mined resources from the provided mine.
     * @param mine Mine the lorry is associated with
     * @param id Id
     */
    public Lorry(Mine mine, int id) {
        TIMESTAMP_IS_CREATED = System.currentTimeMillis();
        this.mine = mine;
        this.ID = id;
        loadedResourcesCount = 0;
    }

    /**
     * Represents Lorry execution logic process.
     */
    @Override
    public void run() {
        long goToFerryDuration = goToFerry();
        mine.logEvent(getClass().getSimpleName(), ID, String.format(
                "Lorry has arrived at the ferry.;duration=%d",
                goToFerryDuration
        ));
        transportViaFerry();
        long goToDestinationDuration = goToDestination();
        mine.logEvent(getClass().getSimpleName(), ID, String.format(
                "Lorry has arrived at the destination.;duration=%d",
                goToDestinationDuration
        ));
        unloadResources();
    }

    /**
     * Generates and returns a random value from (0, tLorry>.
     * @return Generated random value.
     */
    @Override
    public Integer getRandom() {
        return mine.RANDOM.nextInt(mine.INPUT_PARAMETERS.tLorry) + 1;
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Loads a resource onto the lorry.
     * - Increments the number of loaded resources onto the ferry.
     */
    public void loadResource() {
        synchronized (mine.ACCESS_TO_LORRY) {
            if (loadedResourcesCount >= mine.INPUT_PARAMETERS.capLorry) {
                throw new IllegalStateException("Loading a resource over the capacity.");
            }

            loadedResourcesCount++;
            if (loadedResourcesCount == mine.INPUT_PARAMETERS.capLorry) {
                timestampIsFull = System.currentTimeMillis();
            }
        }
    }

    /**
     * Unloads loaded resources.
     * - Increases mine delivered resources count by number of loaded resources.
     */
    private void unloadResources() {
        if (loadedResourcesCount <= 0) {
            throw new IllegalStateException("Lorry can only unload positive loaded resources count.");
        }

        mine.increaseDeliveredResourcesCountBy(loadedResourcesCount);
        loadedResourcesCount = 0;
    }

    /**
     * Lorry goes to the ferry.
     * (SIMULATION-Thread sleeps for the time it takes the lorry to get to the ferry.)
     * @return Time [ms] it took to get to the ferry.
     */
    private long goToFerry() {
        return simulateRide();
    }

    /**
     * Lorry goes to the destination.
     * (SIMULATION-Thread sleeps for the time it takes the lorry to get to the destination.)
     * @return Time [ms] it took to get to the destination.
     */
    private long goToDestination() {
        return simulateRide();
    }

    /**
     * Lorry simulates a ride.
     * (SIMULATION-Thread sleeps for the time of the ride of the lorry.)
     * @return Time [ms] the ride took.
     */
    private long simulateRide() {
        long duration = getRandom();
        mine.simulateAction(duration);

        return duration;
    }

    /**
     * Lorry lets Ferry handle the transport of the lorry.
     */
    private void transportViaFerry() {
        Ferry ferry = mine.getFerry();
        ferry.handleTransport();
    }

    /**
     * Returns the id of the lorry.
     * @return Id of the lorry
     */
    public int getId() {
        return ID;
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Returns whether the lorry is empty.
     * @return True, if the lorry is empty, else false.
     */
    public boolean isEmpty() {
        synchronized (mine.ACCESS_TO_LORRY) {
            return loadedResourcesCount == 0;
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Returns whether the lorry has been fully loaded.
     * @return True, if the lorry is full, else false.
     */
    public boolean isFull() {
        synchronized (mine.ACCESS_TO_LORRY) {
            return loadedResourcesCount == mine.INPUT_PARAMETERS.capLorry;
        }
    }

    /**
     * Returns the timestamp of the lorry creation.
     * @return Timestamp of the lorry creation
     */
    public long getTimestampIsCreated() {
        return TIMESTAMP_IS_CREATED;
    }

    /**
     * Returns the time [ms] it took to fill the lorry since its creation.
     * @return Time [ms] it took to fill the lorry since its creation
     */
    public long getDurationIsFull() {
        return timestampIsFull - TIMESTAMP_IS_CREATED;
    }

}
