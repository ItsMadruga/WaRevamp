package its.madruga.warevamp.module.references;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReferencesUtils {

    public static Field getFieldByType(Class<?> cls, Class<?> type) {
        return Arrays.stream(cls.getFields()).filter(f -> type == f.getType()).findFirst().orElse(null);
    }

    public static Field getFieldByExtendType(Class<?> cls, Class<?> type) {
        return Arrays.stream(cls.getFields()).filter(f -> type.isAssignableFrom(f.getType())).findFirst().orElse(null);
    }

    public static List<Field> getFieldsByExtendType(Class<?> cls, Class<?> type) {
        return Arrays.stream(cls.getFields()).filter(f -> type.isAssignableFrom(f.getType())).collect(Collectors.toList());
    }

    public static Object getObjectField(Field loadProfileInfoField, Object thisObject) {
        try {
            return loadProfileInfoField.get(thisObject);
        } catch (Exception e) {
            return null;
        }
    }

    public static Method findMethodUsingFilter(Class<?> clazz, Predicate<Method> predicate) {
        do {
            var results = Arrays.stream(clazz.getDeclaredMethods()).filter(predicate).findFirst();
            if (results.isPresent()) return results.get();
        } while ((clazz = clazz.getSuperclass()) != null);
        throw new RuntimeException("Method not found");
    }

    public static Field findFieldUsingFilter(Class<?> clazz, Predicate<Field> predicate) {
        do {
            var results = Arrays.stream(clazz.getDeclaredFields()).filter(predicate).findFirst();
            if (results.isPresent()) return results.get();
        } while ((clazz = clazz.getSuperclass()) != null);
        throw new RuntimeException("Field not found");
    }

    public synchronized static boolean isCalledFromMethod(Method method) {
        var trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : trace) {
            if (stackTraceElement.getClassName().equals(method.getDeclaringClass().getName()) && stackTraceElement.getMethodName().equals(method.getName()))
                return true;
        }
        return false;
    }

    public static Object[] initArray(Class<?>[] parameterTypes) {
        var args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = getDefaultValue(parameterTypes[i]);
        }
        return args;
    }

    public static Object getDefaultValue(Class<?> paramType) {
        if (paramType == int.class || paramType == Integer.class) {
            return 0;
        } else if (paramType == long.class || paramType == Long.class) {
            return 0L;
        } else if (paramType == double.class || paramType == Double.class) {
            return 0.0;
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            return false;
        }
        return null;
    }

}
