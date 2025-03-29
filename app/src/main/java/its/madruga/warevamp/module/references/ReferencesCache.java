package its.madruga.warevamp.module.references;

import android.content.Context;
import android.content.SharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.BuildConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class ReferencesCache {
    private static Context context;
    private static SharedPreferences preferences;
    private static ClassLoader loader;

    public static void init(Context ctx, ClassLoader classLoader) {
        context = ctx;
        preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID + "_references", Context.MODE_PRIVATE);
        loader = classLoader;
        checkWppVersion();
    }

    public static Map<String, Class<?>> primitiveClasses = Map.of(
            "byte", Byte.TYPE,
            "short", Short.TYPE,
            "int", Integer.TYPE,
            "long", Long.TYPE,
            "float", Float.TYPE,
            "boolean", Boolean.TYPE
    );

    private static String getMethodPathString(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName());
        builder.append("~");
        builder.append(method.getName());
        if (method.getParameterTypes().length > 0) {
            builder.append("~");
            builder.append(Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(":")));
        }
        builder.delete(builder.length(), builder.length());
        return builder.toString();
    }

    public static Method getMethod(String hookName) {
        String path = preferences.getString(hookName, "");
        if (path.isEmpty()) {
            return null;
        }

        String[] clazzAndMethod = path.split("~");
        Class<?> clazz = XposedHelpers.findClass(clazzAndMethod[0], loader);
        if (clazzAndMethod.length >= 3) {
            String[] parameters = clazzAndMethod[2].split(":");
            Class<?>[] parameterTypes = Arrays.stream(parameters).map(ReferencesCache::findClass).toArray(Class<?>[]::new);
            return XposedHelpers.findMethodExact(clazz, clazzAndMethod[1], parameterTypes);
        }
        return XposedHelpers.findMethodExact(clazz, clazzAndMethod[1]);
    }

    private static String getConstructorPathString(Constructor<?> constructor) {
        StringBuilder builder = new StringBuilder();
        builder.append(constructor.getDeclaringClass().getName());
        if (constructor.getParameterTypes().length > 0) {
            builder.append("~");
            builder.append(Arrays.stream(constructor.getParameterTypes()).map(Class::getName).collect(Collectors.joining(":")));
        }
        builder.delete(builder.length(), builder.length());
        return builder.toString();
    }

    public static Constructor<?> getConstructor(String hookName) {
        String path = preferences.getString(hookName, "");
        if (path.isEmpty()) {
            return null;
        }

        String[] clazzAndConstructor = path.split("~");
        Class<?> clazz = XposedHelpers.findClass(clazzAndConstructor[0], loader);
        if (clazzAndConstructor.length >= 3) {
            String[] parameters = clazzAndConstructor[2].split(":");
            Class<?>[] parameterTypes = Arrays.stream(parameters).map(ReferencesCache::findClass).toArray(Class<?>[]::new);
            return XposedHelpers.findConstructorExact(clazz, parameterTypes);
        }
        return XposedHelpers.findConstructorExact(clazz);
    }

    public static Method[] getMethods(String hookName) {
        Set<String> path = preferences.getStringSet(hookName, null);
        if (path == null) {
            return null;
        }
        List<String> pathList = new ArrayList<>(path);
        Method[] methods = new Method[pathList.size()];
        for (int i = 0; i < pathList.size(); i++) {
            String[] clazzAndMethod = pathList.get(i).split("~");
            Class<?> clazz = XposedHelpers.findClass(clazzAndMethod[0], loader);
            if (clazzAndMethod.length >= 3) {
                String[] parameters = clazzAndMethod[2].split(":");
                Class<?>[] parameterTypes = Arrays.stream(parameters).map(ReferencesCache::findClass).toArray(Class<?>[]::new);
                methods[i] = XposedHelpers.findMethodExact(clazz, clazzAndMethod[1], parameterTypes);
                continue;
            }
            methods[i] = XposedHelpers.findMethodExact(clazz, clazzAndMethod[1]);
        }
        return methods;
    }

    public static Class<?> findClass(String className) {
        Class<?> primitiveClass = primitiveClasses.get(className);
        if (primitiveClass != null) return primitiveClass;
        return XposedHelpers.findClass(className, loader);
    }

    public static Class<?> getClazz(String hookName) {
        if (preferences == null) return null;
        String classPath = preferences.getString(hookName, "");
        if (classPath.isEmpty()) return null;
        return XposedHelpers.findClass(classPath, loader);
    }

    public static Field getField(String hookName) {
        String fieldPath = preferences.getString(hookName, "");
        if (fieldPath.isEmpty()) return null;
        String[] clazzAndField = fieldPath.split("~");
        return XposedHelpers.findField(XposedHelpers.findClass(clazzAndField[0], loader), clazzAndField[1]);
    }

    public static void saveMethodPath(Method method, String hookName) {
        preferences.edit().putString(hookName, getMethodPathString(method)).apply();
    }

    public static void saveMethodsPath(Method[] methods, String hookName) {
        HashSet<String> methodInfo = new HashSet<>();
        for (Method method : methods) {
            methodInfo.add(getMethodPathString(method));
        }
        preferences.edit().putStringSet(hookName, methodInfo).apply();
    }

    public static void saveConstructor(Constructor<?> constructor, String hookName) {
        preferences.edit().putString(hookName,getConstructorPathString(constructor));
    }

    public static void saveClassPath(Class<?> clazz, String hookName) {
        preferences.edit().putString(hookName, clazz.getName()).apply();
    }

    public static void saveFieldPath(Field field, String hookName) {
        String builder =
                field.getDeclaringClass().getName() +
                    "~" +
                field.getName();
        preferences.edit().putString(hookName, builder).apply();
    }


    private static void checkWppVersion() {
        try {
        String versionCache = preferences.getString("version", "");
        String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        if (versionCache.isEmpty()) preferences.edit().putString("version", version).apply();
        if (!version.equals(versionCache)) {
            preferences.edit().clear().apply();
            preferences.edit().putString("version", version).apply();
        }
        } catch (Exception ignored) {}
    }
}
