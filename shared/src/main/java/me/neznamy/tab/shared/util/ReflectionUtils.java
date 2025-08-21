package me.neznamy.tab.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class storing methods working with reflection
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

    /**
     * Returns {@code true} if class with given full name exists,
     * {@code false} if not.
     *
     * @param   path
     *          Full class path and name
     * @return  {@code true} if exists, {@code false} if not
     */
    public static boolean classExists(@NotNull String path) {
        try {
            Class.forName(path);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Returns {@code true} if method with specified name and parameters exists in
     * the class, {@code false} if not.
     *
     * @param   clazz
     *          Class where the method is
     * @param   method
     *          Name of the method
     * @param   parameterTypes
     *          Method parameters
     * @return  {@code true} if exists, {@code false} if not
     */
    public static boolean methodExists(@NotNull Class<?> clazz, @NotNull String method, @NotNull Class<?>... parameterTypes) {
        try {
            clazz.getMethod(method, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Returns all fields of class with defined class type
     *
     * @param   clazz
     *          class to check fields of
     * @param   type
     *          field type to check for
     * @return  list of all fields with specified class type
     */
    @NotNull
    public static List<Field> getFields(@NotNull Class<?> clazz, @NotNull Class<?> type) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                list.add(setAccessible(field));
            }
        }
        return list;
    }

    /**
     * Returns list of instance fields matching class type
     *
     * @param   clazz
     *          Class to get instance fields of
     * @param   fieldType
     *          Type of field
     * @return  List of instance fields with defined class type
     */
    @NotNull
    public static List<Field> getInstanceFields(@NotNull Class<?> clazz, @NotNull Class<?> fieldType) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == fieldType && !Modifier.isStatic(field.getModifiers())) {
                list.add(setAccessible(field));
            }
        }
        return list;
    }

    /**
     * Makes object accessible.
     *
     * @param   o
     *          Object to make accessible
     * @return  Provided object
     * @param   <T>
     *          AccessibleObject
     */
    @NotNull
    public static <T extends AccessibleObject> T setAccessible(@NotNull T o) {
        o.setAccessible(true);
        return o;
    }

    /**
     * Returns the only constructor of specified class. If class has more than 1
     * constructor, IllegalStateException is thrown.
     *
     * @param   clazz
     *          Class to get constructor of
     * @return  The one and only constructor of the class
     * @throws  IllegalStateException
     *          If class has more than 1 constructor or doesn't have any
     */
    @NotNull
    public static Constructor<?> getOnlyConstructor(@NotNull Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length != 1) {
            throw new IllegalStateException("Class " + clazz.getName() + " is expected to have 1 constructor, but has " +
                    constructors.length + ": \n" + Arrays.stream(constructors).map(Constructor::toString).collect(Collectors.joining("\n")));
        }
        return constructors[0];
    }

    /**
     * Returns the one and only field of class with defined class type. Throws
     * IllegalStateException if more than 1 or no fields were found.
     *
     * @param   clazz
     *          class to check fields of
     * @param   type
     *          field type to check for
     * @return  The one and only field with defined class type
     * @throws  IllegalStateException
     *          If more than 1 field meets the criteria or if none do.
     */
    @NotNull
    public static Field getOnlyField(@NotNull Class<?> clazz, @NotNull Class<?> type) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                list.add(setAccessible(field));
            }
        }
        if (list.size() != 1) {
            throw new IllegalStateException("Class " + clazz.getName() + " is expected to have 1 field of type " + type.getName() + ", but has " +
                    list.size() + ": " + list.stream().map(Field::getName).collect(Collectors.toList()));

        }
        return list.get(0);
    }

    /**
     * Returns field from given possible names
     *
     * @param   clazz
     *          Class to get field from
     * @param   names
     *          All possible names of the field
     * @return  Field from given potential names
     * @throws  IllegalArgumentException
     *          If no such field is found
     */
    @NotNull
    public static Field getField(@NotNull Class<?> clazz, @NotNull String... names) {
        for (String name : names) {
            try {
                return setAccessible(clazz.getDeclaredField(name));
            } catch (NoSuchFieldException ignored) {}
        }
        throw new IllegalArgumentException("Class " + clazz.getName() + " does not contain a field with potential names " +
                Arrays.toString(names));
    }
}
