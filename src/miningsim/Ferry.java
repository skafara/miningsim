package miningsim;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Represents a ferry transporting lorries over the river.
 * @author Stanislav Kafara
 * @version 1
 */
public class Ferry {

    private final Mine mine;

    private static final String LOG_DEPART = "Ferry has departed from the origin shore.;duration=%d";

    /** Timestamp of the ferry being docked on the origin shore. */
    private long timestampIsDocked;
    /** Timestamp of the ferry being departed from the origin shore. */
    private long timestampIsDeparted;

    /** Barrier pausing the threads of the lorries to wait for each other
     * until the ferry capacity is reached and the ferry may depart. */
    private final CyclicBarrier WAITING_FOR_OTHERS;
    /** Semaphore of capFerry permits allowing only capFerry lorries
     * interact with the ferry at the moment. */
    private final Semaphore ACCESS_TO_FERRY;

    /**
     * Creates a ferry transporting lorries from the provided mine.
     * @param mine Mine the ferry is associated with
     */
    public Ferry(Mine mine) {
        this.mine = mine;

        WAITING_FOR_OTHERS = new CyclicBarrier(mine.INPUT_PARAMETERS.capFerry, this::transport);
        ACCESS_TO_FERRY = new Semaphore(mine.INPUT_PARAMETERS.capFerry);
        dock();
    }

    /**
     * Handles the transport of the lorry from the origin shore to the destination shore.
     * (WARNING-This method must be called by the threads of the lorries.)
     * (INFO-If necessary, pauses the thread of the lorry until it may board onto the ferry.
     * This happens when the ferry is in transport.)
     * (INFO-If necessary, pauses the thread until the ferry departs.
     * This happens when the lorry is not full yet.)
     * (INFO-Threads of the lorries are resumed after the lorries were transported.)
     */
    public void handleTransport() {
        // Lorry waits until it gets access to board onto the ferry.
        ACCESS_TO_FERRY.acquireUninterruptibly();
        try {
            // Lorry boards onto the ferry and waits until capFerry lorries are on board.
            WAITING_FOR_OTHERS.await(); // After that actual transport occurs.
            // End of the ferry transport action. Ferry is back at the origin shore.
        } catch (InterruptedException | BrokenBarrierException e) {
            //
        }
        // Ferry releases the permits of lorries that already got off the ferry at the destination shore
        // enabling others to board onto the ferry at the origin shore.
        ACCESS_TO_FERRY.release();
    }

    /**
     * Transports the lorries to the destination shore and returns to the origin shore.
     * (INFO-This method is called by the Barrier WAITING_FOR_OTHERS when capFerry lorries board onto the ferry.)
     * (INFO-This action is atomic.)
     */
    private void transport() {
        depart();
        mine.logConsoleEvent(getClass().getSimpleName(), String.format(LOG_DEPART, getDurationIsDeparted()));
        mine.logEvent(getClass().getSimpleName(), String.format(LOG_DEPART, getDurationIsDeparted()));
        // Ferry goes to the destination shore.
        // Ferry arrives at the destination shore.
        // Lorries get off the ferry and their threads are resumed.
        // Ferry goes back to the origin shore.
        // Ferry arrives at the origin shore.
        dock();
    }

    /**
     * Departs the ferry from the origin shore.
     * - Updates the timestamp of the ferry being departed from the origin shore.
     */
    private void depart() {
        timestampIsDeparted = System.currentTimeMillis();
    }

    /**
     * Docks the ferry on the origin shore.
     * - Updates the timestamp of the ferry being docked on the origin shore.
     */
    private void dock() {
        timestampIsDocked = System.currentTimeMillis();
    }

    /**
     * Returns the time [ms] it took the ferry
     * to depart from the origin shore since last docking on the origin shore.
     * @return Time [ms] it took the ferryto depart from the origin shore since last docking on the origin shore
     */
    public long getDurationIsDeparted() {
        return timestampIsDeparted - timestampIsDocked;
    }

}
