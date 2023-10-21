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
        } catch (ClassNotFoundException | NullPointerException e) {
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
     * Returns all methods from class which return specified class type and have specified parameter types.
     *
     * @param   clazz
     *          Class to get methods from
     * @param   returnType
     *          Return type of methods
     * @param   parameterTypes
     *          Parameter types of methods
     * @return  List of found methods matching requirements. If nothing is found, empty list is returned.
     */
    @NotNull
    public static List<Method> getMethods(@NotNull Class<?> clazz, @NotNull Class<?> returnType, @NotNull Class<?>... parameterTypes) {
        List<Method> list = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (!returnType.isAssignableFrom(m.getReturnType()) || m.getParameterCount() != parameterTypes.length) continue;
            Class<?>[] types = m.getParameterTypes();
            boolean valid = true;
            for (int i=0; i<types.length; i++) {
                if (types[i] != parameterTypes[i]) {
                    valid = false;
                    break;
                }
            }
            if (valid) list.add(m);
        }
        return list;
    }

    /**
     * Returns method with specified possible names and parameters. Throws exception if no such method was found
     *
     * @param   clazz
     *          lass to get method from
     * @param   names
     *          possible method names
     * @param   parameterTypes
     *          parameter types of the method
     * @return  method with specified name and parameters
     * @throws  NoSuchMethodException
     *          if no such method exists
     */
    @NotNull
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String[] names, @NotNull Class<?>... parameterTypes) throws NoSuchMethodException {
        List<String> list = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getParameterCount() != parameterTypes.length) continue;
            Class<?>[] types = m.getParameterTypes();
            boolean valid = true;
            for (int i=0; i<types.length; i++) {
                if (types[i] != parameterTypes[i]) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                for (String name : names) {
                    if (m.getName().equals(name)) return m;
                    String[] array = m.getName().split("_");
                    if (array.length > 2 && array[2].equals(name)) return m; // Bukkit/Forge hybrids may sometimes use these mappings
                }
                list.add(m.getName());
            }
        }
        throw new NoSuchMethodException("No method found with possible names " + Arrays.toString(names) + " with parameters " +
                Arrays.toString(parameterTypes) + " in class " + clazz.getName() + ". Methods with matching parameters: " + list);
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
    public static Constructor<?> getOnlyConstructor(@NotNull Class<?> clazz) throws IllegalStateException {
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
    public static Field getOnlyField(@NotNull Class<?> clazz, @NotNull Class<?> type) throws IllegalStateException {
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
     * Returns the one and only method of class with defined return type and parameters. Throws
     * IllegalStateException if more than 1 or no methods were found.
     *
     * @param   clazz
     *          class to check fields of
     * @param   returnType
     *          field type to check for
     * @param   parameterTypes
     *          Parameter types
     * @return  The one and only method with defined return type and parameter
     * @throws  IllegalStateException
     *          If more than 1 methods meets the criteria or if none do.
     */
    @NotNull
    public static Method getOnlyMethod(@NotNull Class<?> clazz, @NotNull Class<?> returnType, @NotNull Class<?>... parameterTypes) {
        List<Method> list = getMethods(clazz, returnType, parameterTypes);
        if (list.size() != 1) {
            throw new IllegalStateException("Class " + clazz.getName() + " is expected to have 1 method with return type " +
                    returnType.getName() + " and parameters " + Arrays.toString(parameterTypes) + ", but has " +
                    list.size() + ": \n" + list.stream().map(Method::toString).collect(Collectors.joining("\n")));

        }
        return list.get(0);
    }

    /**
     * Returns the only field in the class. If it has more, exception is thrown.
     *
     * @param   clazz
     *          Class to get field of
     * @return  The one and only field of the class
     * @throws  IllegalStateException
     *          If class has more than 1 field or has none
     */
    @NotNull
    public static Field getOnlyField(@NotNull Class<?> clazz) throws IllegalStateException{
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length != 1) {
            throw new IllegalStateException("Class " + clazz.getName() + " is expected to have 1 field, but has " +
                    fields.length + ": " + Arrays.stream(fields).map(Field::getName).collect(Collectors.toList()));
        }
        return setAccessible(fields[0]);
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
    public static Field getField(@NotNull Class<?> clazz, @NotNull String... names) throws IllegalArgumentException {
        for (String name : names) {
            try {
                return setAccessible(clazz.getDeclaredField(name));
            } catch (NoSuchFieldException ignored) {}
        }
        throw new IllegalArgumentException("Class " + clazz.getName() + " does not contain a field with potential names " +
                Arrays.toString(names));
    }
}
