package me.neznamy.tab.api.util;

/**
 * Supplier that can throw an exception
 *
 * @param   <T>
 *          Return class type
 */
public interface SupplierWithException<T> {

    T get() throws Exception;
}
