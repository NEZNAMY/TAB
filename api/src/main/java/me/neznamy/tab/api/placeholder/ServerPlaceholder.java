package me.neznamy.tab.api.placeholder;

public interface ServerPlaceholder extends Placeholder {

    void updateValue(Object value);

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and "ERROR" is returned.
     *
     * @return  value placeholder returned or "ERROR" if it threw an error
     */
    Object request();

    /**
     * Returns last known value of the placeholder.
     *
     * @return  last known value
     */
    String getLastValue();
}