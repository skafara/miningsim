package miningsim;

import miningsim.utils.IRandomGeneration;

import java.util.NoSuchElementException;

/**
 * Represents a mine worker and its execution logic process.
 * @author Stanislav Kafara
 * @version 1
 */
public class Worker implements Runnable, IRandomGeneration<Integer> {

    /** Time [ms] it takes the worker to load a resource onto a ferry. */
    private static final long LOAD_RESOURCE_DURATION_MS = 1;

    /** Mine the Worker is associated with */
    private final Mine mine;
    /** Worker ID */
    private final int ID;

    /** Number of resources in the assigned block of resources */
    private int assignedBlockResourcesCount;
    /** Number of resources the worker is carrying.
     * These are mined resource but not yet loaded onto a lorry. */
    private int carryingResourcesCount;
    /** Number of mined resources */
    private int totalResourcesMinedCount;

    /**
     * Creates a worker with provided id working in the provided mine.
     * @param mine Mine the Worker is associated with
     * @param id Id
     */
    public Worker(Mine mine, int id) {
        this.mine = mine;
        this.ID = id;
        assignedBlockResourcesCount = 0;
        carryingResourcesCount = 0;
        totalResourcesMinedCount = 0;
    }

    /**
     * Represents Worker execution logic process.
     */
    @Override
    public void run() {
        Foreman foreman = mine.getForeman();
        try {
            while (true) {
                // Worker gets a block of resources, if there is any from the foreman.
                assignedBlockResourcesCount = foreman.getNextResourcesBlock();
                // Worker mines the assigned block of resources.
                long mineResourceBlockDuration = mineResourcesBlock();
                mine.logEvent(getClass().getSimpleName(), ID, String.format(
                        "Finished mining a block of resources.;duration=%d",
                        mineResourceBlockDuration
                ));
                // Worker carries the mined resources to the lorry.
                // Worker loads the mined resources onto the lorry.
                loadResources();
            }
        }
        catch (NoSuchElementException e) {
            // There are not any more blocks of resources to mine.
        }
    }

    /**
     * Generates and returns a random value from (0, tWorker>.
     * @return Generated random value.
     */
    @Override
    public Integer getRandom() {
        return mine.RANDOM.nextInt(mine.INPUT_PARAMETERS.tWorker) + 1;
    }

    /**
     * Mines assigned block of resources and returns the time [ms] it took.
     * @return Time [ms] it took to mine the block of resources.
     */
    private long mineResourcesBlock() {
        if (assignedBlockResourcesCount <= 0) {
            throw new IllegalStateException("Mining a resource block with not positive resources count.");
        }

        long mineResourceBlockDuration = 0;
        for (; assignedBlockResourcesCount > 0; assignedBlockResourcesCount--) {
            long mineResourceDuration = mineResource();
            mine.logEvent(getClass().getSimpleName(), ID, String.format(
                    "Finished mining a resource.;duration=%d",
                    mineResourceDuration
            ));
            mineResourceBlockDuration += mineResourceDuration;
        }

        return mineResourceBlockDuration;
    }

    /**
     * Mines a resource and returns the time [ms] it took.
     * (SIMULATION-Thread sleeps for the time it takes the miner to mine the resource.)
     * @return Time [ms] it took to mine the resource.
     */
    private long mineResource() {
        if (assignedBlockResourcesCount <= 0) {
            throw new IllegalStateException("Mining a resource outside the range of the assigned block.");
        }

        long duration = getRandom();
        mine.simulateAction(duration);
        carryingResourcesCount++;
        totalResourcesMinedCount++;

        return duration;
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Loads the carried resources onto the lorry.
     * If Worker fills up the Lorry during loading the resources,
     * Worker departs the lorry and prepares a new one in the mine.
     */
    private void loadResources() {
        for (; carryingResourcesCount > 0; carryingResourcesCount--) {
            // Worker acquires exclusive access to work with the mine lorry.
            synchronized (mine.ACCESS_TO_LORRY) {
                // Worker loads a resource onto the mine lorry.
                loadResource();
                // Worker checks whether the lorry is full.
                Lorry lorry = mine.getLorry();
                if (lorry.isFull()) {
                    mine.logEvent(Lorry.class.getSimpleName(), lorry.getId(), String.format(
                            "Lorry has been filled.;duration=%d",
                            lorry.getDurationIsFull()
                    ));
                    // Worker departs the lorry.
                    departLorry();
                    // Worker prepares a new lorry in the mine.
                    prepareLorry();
                }
            }
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Loads a resource onto the lorry.
     * (SIMULATION-Thread sleeps for the time it takes the miner to load a resource onto the lorry.)
     */
    private void loadResource() {
        synchronized (mine.ACCESS_TO_LORRY) {
            mine.simulateAction(LOAD_RESOURCE_DURATION_MS);
            Lorry lorry = mine.getLorry();
            lorry.loadResource();
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Departs the mine lorry.
     */
    private void departLorry() {
        synchronized (mine.ACCESS_TO_LORRY) {
            mine.departLorry();
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_LORRY) Prepares a new lorry in the mine.
     */
    private void prepareLorry() {
        synchronized (mine.ACCESS_TO_LORRY) {
            mine.prepareLorry();
        }
    }

    /**
     * Returns the id of the worker.
     * @return Id of the worker
     */
    public int getId() {
        return ID;
    }

    /**
     * Returns number of mined resources.
     * @return Number of mined resources
     */
    public int getTotalResourcesMinedCount() {
        return totalResourcesMinedCount;
    }

}
