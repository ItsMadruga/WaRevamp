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

    public synchronized static boolean isCalledFromMethod(Method method) {
        var trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : trace) {
            if (stackTraceElement.getClassName().equals(method.getDeclaringClass().getName()) && stackTraceElement.getMethodName().equals(method.getName()))
                return true;
        }
        return false;
    }
}
