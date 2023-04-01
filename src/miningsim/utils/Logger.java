package miningsim.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Represents a logger.
 * @author Stanislav Kafara
 * @version 1
 */
public class Logger {

    /** Path to the file the logs will be flushed to. */
    private final Path pathToFile;
    /** Logs will be flushed to the file with openOption option. */
    private final StandardOpenOption[] openOptions;

    /** Logs not yet flushed to the file */
    private final Deque<String> logs;

    /**
     * Creates a logger flushing the logs to the provided file.
     * @param fileName File name
     * @param openOptions File open options
     */
    public Logger(String fileName, StandardOpenOption... openOptions) {
        pathToFile = Paths.get(fileName);
        this.openOptions = openOptions;
        logs = new LinkedList<>();
    }

    /**
     * (SYNCHRONIZED-Logger) Registers a log for later flushing to the file.
     * @param log Log
     */
    public synchronized void log(String log) {
        logs.addLast(log);
    }

    /**
     * (SYNCHRONIZED-Logger) Flushes the logs to the file.
     * @throws IOException If any problem occurred during flushing the logs to the file.
     */
    public synchronized void flush() throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(pathToFile, openOptions)) {
            while (!logs.isEmpty()) {
                String log = logs.removeFirst();
                bufferedWriter.write(log);
                bufferedWriter.newLine();
            }
        }
    }

}
