package miningsim;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

/**
 * Launcher of the miningsim application
 * @author Stanislav Kafara
 * @version 1
 */
public class Main {

    /** Console error message format */
    private static final String ERROR_FORMAT = "miningsim[ERROR]: %s%n";

    /** Console log divider */
    public static final String LOG_DIVIDER = "--------------------------------------------------";

    private Main() {}

    /**
     * Entry point of the launcher
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Prints info about the application.
        printApplicationInfo();
        try {
            // Extracts command line arguments.
            InputParameters inputParameters = InputParameters.extract(args);

            // Prints extracted command line arguments.
            printInputParameters(inputParameters);

            Mine mine = new Mine(inputParameters);
            Thread threadMine = new Thread(mine);

            threadMine.start(); // Launches mining application logic process.
            threadMine.join();
            if (mine.getThrownException() != null) {
                // An exception was caught during execution of mining process.
                throw mine.getThrownException(); // Propagate the exception to Main.
            }
        }
        catch (IllegalArgumentException e) {
            System.err.format(ERROR_FORMAT, e.getMessage());
        }
        catch (NoSuchFileException e) {
            System.err.format(ERROR_FORMAT, String.format("File %s does not exist.", e.getMessage()));
        }
        catch (IOException e) {
            System.err.format(ERROR_FORMAT, "Unexpected problem occurred during file system manipulation.");
        }
        catch (InterruptedException e) {
            //
        }
        catch (Exception e) {
            System.err.format(ERROR_FORMAT, "Unexpected problem occurred during program execution.");
        }
    }

    /**
     * Prints info about the application.
     */
    private static void printApplicationInfo() {
        System.out.println("miningsim - Mining Simulator (2023-03-25, v1)");
        System.out.println(LOG_DIVIDER);
        System.out.println("Seminar Work of KIV/PGS - \"Programming Structures\"");
        System.out.println("Stanislav Kafara, skafara@students.zcu.cz");
        System.out.println("University of West Bohemia, Pilsen");
        System.out.println(LOG_DIVIDER);
    }

    /**
     * Prints input parameters.
     * @param inputParameters Input parameters
     */
    private static void printInputParameters(InputParameters inputParameters) {
        System.out.println("Miningsim Input Arguments:");
        printInputParameter(InputParameters.VALID_SWITCHES[0], inputParameters.i);
        printInputParameter(InputParameters.VALID_SWITCHES[1], inputParameters.o);
        printInputParameter(InputParameters.VALID_SWITCHES[2], String.valueOf(inputParameters.cWorker));
        printInputParameter(InputParameters.VALID_SWITCHES[3], String.valueOf(inputParameters.tWorker));
        printInputParameter(InputParameters.VALID_SWITCHES[4], String.valueOf(inputParameters.capLorry));
        printInputParameter(InputParameters.VALID_SWITCHES[5], String.valueOf(inputParameters.tLorry));
        printInputParameter(InputParameters.VALID_SWITCHES[6], String.valueOf(inputParameters.capFerry));
        System.out.println(LOG_DIVIDER);
    }

    /**
     * Prints input parameter and it value.
     * @param inputParameterSwitch Input parameter switch
     * @param value Parameter value
     */
    private static void printInputParameter(String inputParameterSwitch, String value) {
        System.out.format("%-9s:\t%s%n", inputParameterSwitch, value);
    }

    /**
     * Represents miningsim application parameters.
     * @author Stanislav Kafara
     * @version 1
     */
    public static class InputParameters {

        /** Expected command line arguments count */
        private static final int VALID_ARGUMENTS_COUNT = 14;

        /** Expected switches with given order */
        private static final String[] VALID_SWITCHES = new String[] {
                "-i", "-o", "-cWorker", "-tWorker", "-capLorry", "-tLorry", "-capFerry"
        };

        /** Path to the input file */
        public final String i;
        /** Path to the output file */
        public final String o;
        /** Workers count */
        public final int cWorker;
        /** Max time [ms] it takes the worker to mine a resource. */
        public final int tWorker;
        /** Lorry resources capacity */
        public final int capLorry;
        /** Max time [ms] it takes the lorry to get to the ferry. */
        public final int tLorry;
        /** Ferry lorries capacity */
        public final int capFerry;

        private InputParameters(String i, String o, int cWorker, int tWorker, int capLorry, int tLorry, int capFerry) {
            this.i = i;
            this.o = o;
            this.cWorker = cWorker;
            this.tWorker = tWorker;
            this.capLorry = capLorry;
            this.tLorry = tLorry;
            this.capFerry = capFerry;
        }

        /**
         * Extracts miningsim application parameters from command line arguments.
         * @param args Command line arguments
         * @return Extracted mininingsim application parameters
         * @throws IllegalArgumentException If miningsim application parameters could not be extracted.
         */
        private static InputParameters extract(String[] args) throws IllegalArgumentException {
            if (!isValidArgumentsCount(args)) {
                throw new IllegalArgumentException("Invalid arguments count");
            }
            if (!isValidSwitches(args)) {
                throw new IllegalArgumentException("Invalid arguments switches");
            }

            try {
                String i = args[1];
                String o = args[3];
                int cWorker = Integer.parseInt(args[5]);
                int tWorker = Integer.parseInt(args[7]);
                int capLorry = Integer.parseInt(args[9]);
                int tLorry = Integer.parseInt(args[11]);
                int capFerry = Integer.parseInt(args[13]);

                if (!isValidArgumentsValues(cWorker, tWorker, capLorry, tLorry, capFerry)){
                    throw new IllegalArgumentException();
                }

                return new InputParameters(i, o, cWorker, tWorker, capLorry, tLorry, capFerry);
            }
            catch (IllegalArgumentException e) {
                // Catches both manually thrown IllegalArgumentException and automatically thrown NumberFormatException.
                throw new IllegalArgumentException("Invalid arguments values");
            }
        }

        /**
         * Checks whether there is proper command line arguments count.
         * @param args Command line arguments
         * @return True, if there is, else false.
         */
        private static boolean isValidArgumentsCount(String[] args) {
            return args.length == VALID_ARGUMENTS_COUNT;
        }

        /**
         * Checks whether expected switches are on their proper position.
         * @param args Command line arguments
         * @return True, if they are, else false.
         */
        private static boolean isValidSwitches(String[] args) {
            for (int i = 0; i < VALID_SWITCHES.length; i++) {
                if (!args[2 * i].equals(VALID_SWITCHES[i])) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Checks whether arguments values are valid, i.e. they are positive numbers.
         * @param cWorker cWorker
         * @param tWorker tWorker
         * @param capLorry capLorry
         * @param tLorry tLorry
         * @param capFerry capFerry
         * @return True, if they are valid, else false.
         */
        private static boolean isValidArgumentsValues(int cWorker, int tWorker, int capLorry, int tLorry, int capFerry) {
            return cWorker >= 1 && tWorker >= 1 && capLorry >= 1 && tLorry >= 1 && capFerry >= 1;
        }

    }

}
