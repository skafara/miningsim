package miningsim.utils;

/**
 * Represents random values generation of type <T>.
 * @param <T> Type of value to be randomly generated.
 * @author Stanislav Kafara
 * @version 1
 */
public interface IRandomGeneration<T> {

    /**
     * Generates and returns a random value.
     * @return Generated random value.
     */
    T getRandom();

}
