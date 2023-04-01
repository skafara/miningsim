package miningsim;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Represents a mine Foreman.
 * @author Stanislav Kafara
 * @version 1
 */
public class Foreman {

    /** Mine the Foreman is associated with */
    private final Mine mine;

    /**
     * Creates a foreman working in the provided mine.
     * @param mine Mine the foreman is associated with
     */
    public Foreman(Mine mine) {
        this.mine = mine;
    }

    /**
     * Reads the map and identifies blocks of resources.
     * @return Identified blocks of resources.
     * @throws IOException If any problem occurred during reading the map.
     */
    public Collection<Integer> processMap() throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(mine.INPUT_PARAMETERS.i))) {
            Collection<Integer> resourcesBlocks = new ArrayList<>();

            String line = null;
            String regex = String.format("%c+", Mine.MAP_VOID_CHAR);
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineBlocks = line.split(regex);
                for (String resourceBlock : lineBlocks) {
                    resourcesBlocks.add(resourceBlock.length());
                }
            }

            return resourcesBlocks;
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_RESOURCES_BLOCKS)
     * Returns whether there is any block of resources left to be assigned to a worker.
     * @return True, if there is any block of resources left to be assigned to a worker.
     */
    public boolean hasNextResourcesBlock() {
        synchronized (mine.ACCESS_TO_RESOURCES_BLOCKS) {
            return mine.hasNextResourcesBlock();
        }
    }

    /**
     * (SYNCHRONIZED-Mine.ACCESS_TO_RESOURCES_BLOCKS) Pops next block of resources to be mined and returns it.
     * (INFO-This method should be used by Workers to get next job.)
     * @return Next block of resources to be mined.
     * @throws NoSuchElementException If there is not any block of resources to be mined.
     */
    public int getNextResourcesBlock() throws NoSuchElementException {
        synchronized (mine.ACCESS_TO_RESOURCES_BLOCKS) {
            return mine.popNextResourcesBlock();
        }
    }

}
