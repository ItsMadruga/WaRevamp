package its.madruga.warevamp.module.references;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.WppUtils;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.FieldData;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.MethodDataList;
import org.luckypray.dexkit.result.UsingFieldData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static its.madruga.warevamp.module.references.ReferencesCache.*;

public class References {

    private static DexKitBridge dexKitBridge;
    private static References ins;

    public References() {}

    static  {
        System.loadLibrary("dexkit");
    }

    public static boolean initDexKit(String sourceDir) {
        try {
            dexKitBridge = DexKitBridge.create(sourceDir);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Method findMethodByString(StringMatchType type, ClassLoader loader, String... args) throws Exception {
        MethodMatcher matcher = new MethodMatcher();
        for (String s : args) {
            matcher.addUsingString(s, type);
        }
        MethodDataList methodDataList = dexKitBridge.findMethod(FindMethod.create().matcher(matcher));
        if (methodDataList.isEmpty()) return null;
        MethodData methodData = methodDataList.get(0);
        if (methodData.isMethod()) {
            return methodData.getMethodInstance(loader);
        }
        throw new NoSuchMethodException();
    }

    public Method[] findMethodsByString(StringMatchType type, ClassLoader loader, String... args) throws Exception {
        MethodMatcher matcher = new MethodMatcher();
        for (String s : args) {
            matcher.addUsingString(s, type);
        }
        MethodDataList methodDataList = dexKitBridge.findMethod(FindMethod.create().matcher(matcher));
        if (methodDataList.isEmpty()) return null;
        return methodDataList.stream().filter(MethodData::isMethod).map(methodData -> {
            try {
                return methodData.getMethodInstance(loader);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }).filter(Objects::nonNull).toArray(Method[]::new);
    }

    public Class<?> findClassByString(StringMatchType type, ClassLoader loader, String... args) throws Exception {
        ClassMatcher matcher = new ClassMatcher();
        for (String s : args) {
            matcher.addUsingString(s, type);
        }
        ClassDataList classDataList = dexKitBridge.findClass(FindClass.create().matcher(matcher));
        if (classDataList.isEmpty()) return null;
        return classDataList.get(0).getInstance(loader);
    }

    public static References getIns() {
        return ins;
    }

    // AntiRevoke

    public synchronized static Class<?> FMessageClass(ClassLoader loader) throws Exception {
        Class<?> result = getClazz("FMessageClass");
        if (result != null) return result;
        result = getIns().findClassByString(StringMatchType.Contains, loader, "FMessage/getSenderUserJid/key.id");
        if (result == null) throw new Exception("FMessage class not found");
        saveClassPath(result, "FMessageClass");
        return result;
    }

    public synchronized static Class<?> keyMessageClass(ClassLoader loader) throws Exception {
        Class<?> result = getClazz("keyMessageClass");
        if (result != null) return result;
        result = getIns().findClassByString(StringMatchType.Contains, loader, "Key(id=", ", isFromMe=");
        if (result == null) throw new Exception("KeyMessage class not found");
        saveClassPath(result, "keyMessageClass");
        return result;
    }

    public synchronized static Field keyMessageField(ClassLoader loader) throws Exception {
        Field result = getField("keyMessageField");
        if (result != null) return result;
        Class<?> keyMessageClass = keyMessageClass(loader);
        Class<?> fMessageClass = FMessageClass(loader);
        result = Arrays.stream(fMessageClass.getDeclaredFields()).filter(f -> f.getType().equals(keyMessageClass) && Modifier.isFinal(f.getModifiers())).findFirst().orElse(null);
        if (result == null) throw new Exception("KeyMessage field not found");
        saveFieldPath(result, "keyMessageField");
        return result;
    }

    public synchronized static Field statusPlaybackField(ClassLoader loader) throws Exception {
        Field result = getField("statusPlaybackField");
        if (result != null) return result;
        Class<?> playbackProgress = XposedHelpers.findClass("com.whatsapp.status.playback.widget.StatusPlaybackProgressView", loader);
        ClassDataList classView = dexKitBridge.findClass(FindClass.create().matcher(
                ClassMatcher.create().methodCount(1).addFieldForType(playbackProgress)
        ));
        if (classView.isEmpty()) throw new Exception("StatusPlaybackView class not found");
        Class<?> clsViewStatus = classView.get(0).getInstance(loader);
        Class<?> playbackFragment = XposedHelpers.findClass("com.whatsapp.status.playback.fragment.StatusPlaybackBaseFragment", loader);
        result = Arrays.stream(playbackFragment.getDeclaredFields()).filter(f -> f.getType().equals(clsViewStatus)).findFirst().orElse(null);
        if (result == null) throw new Exception("StatusPlaybackView field not found");
        saveFieldPath(result, "statusPlaybackField");
        return result;
    }

    public synchronized static Field mediaTypeField(ClassLoader loader) throws Exception {
        Field result = getField("mediaTypeField");
        if (result != null) return result;
        MethodDataList aux = dexKitBridge.findMethod(FindMethod.create().matcher(MethodMatcher.create().addUsingString("conversation/refresh")));
        if (aux.isEmpty()) throw new Exception("mediaTypeField aux method not found");
        ClassData fClass = dexKitBridge.getClassData(FMessageClass(loader));
        List<UsingFieldData> usingFieldData = aux.get(0).getUsingFields();
        for (UsingFieldData field : usingFieldData) {
            FieldData f = field.getField();
            if (f.getDeclaredClass().equals(fClass) && f.getType().getName().equals(int.class.getName())) {
                result = f.getFieldInstance(loader);
            }
        }
        if (result == null) throw new Exception("MediaType field not found");
        saveFieldPath(result, "mediaTypeField");
        return result;
    }

    public synchronized static Method newMessageMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("newMessageMethod");
        if (result != null) return result;
        ClassData fMessageData = dexKitBridge.getClassData(FMessageClass(loader));
        MethodDataList methodDataList = fMessageData.findMethod(new FindMethod()
                .matcher(new MethodMatcher()
                        .addUsingString("\n")
                        .returnType(String.class)));
        if (methodDataList.isEmpty()) {
            methodDataList = fMessageData.findMethod(new FindMethod()
                    .matcher(new MethodMatcher()
                            .paramCount(0)
                            .returnType(String.class)));
            if (methodDataList.isEmpty()) throw new Exception("FMessage method not found");
        }
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "newMessageMethod");
        return result;
    }
    
    public synchronized static Method newMessageWithMediaMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("newMessageWithMediaMethod");
        if (result != null) return result;
        ClassData fMessageData = dexKitBridge.getClassData(FMessageClass(loader));
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod()
                .matcher(new MethodMatcher()
                        .addUsingString("Quoted message chatJid is not specified, parentJid is not a UserJid.", StringMatchType.Contains)
                ));
        for (MethodData m : methodDataList) {
            MethodDataList m2 = m.getInvokes();
            for (MethodData n : m2) {
                if(n.getClassName().equals(fMessageData.getName()) && n.getReturnType().getName().equals(String.class.getName())) result = n.getMethodInstance(loader);
            }
        }
        if (result == null) throw new Exception("newMessageWithMediaMethod not found");
        saveMethodPath(result, "newMessageWithMediaMethod");
        return result;
    }

    public synchronized static Class<?> mediaMessageClass(ClassLoader loader) throws Exception {
        Class<?> result = getClazz("mediaMessageClass");
        if (result != null) return result;
        result = getIns().findClassByString(StringMatchType.Contains, loader, "static.whatsapp.net/downloadable?category=PSA");
        if (result == null) throw new Exception("mediaMessageClass not found");
        saveClassPath(result, "mediaMessageClass");
        return result;
    }

    public synchronized static Method antiRevokeMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("antiRevokeMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "msgstore/edit/revoke");
        if (result == null) throw new Exception("AntiRevoke method not found");
        saveMethodPath(result, "antiRevokeMethod");
        return result;
    }

    public synchronized static Method bubbleMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("bubbleMethod");
        if (result != null) return result;
        Class<?> bubbleClass = getIns().findClassByString(StringMatchType.Contains, loader, "ConversationRow/setUpUserNameInGroupView");
        if (bubbleClass == null) throw new Exception("Bubble class not found");
        result = Arrays.stream(bubbleClass.getMethods()).filter(m -> m.getParameterCount() > 1 && m.getParameterTypes()[0] == ViewGroup.class && m.getParameterTypes()[1] == TextView.class).findFirst().orElse(null);
        if (result == null) throw new Exception("Bubble method not found");
        saveMethodPath(result, "bubbleMethod");
        return result;
    }

    public synchronized static Method unknownStatusPlaybackMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("unknownStatusPlaybackMethod");
        if (result != null) return result;
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod()
                .searchPackages("com.whatsapp.status.playback.fragment")
                .matcher(new MethodMatcher()
                        .addUsingString("xFamilyCrosspostManager", StringMatchType.Contains)
                        .addUsingString("xFamilyGating", StringMatchType.Contains)));
        if (methodDataList.isEmpty()) throw new Exception("unknownStatusPlayback method not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "unknownStatusPlaybackMethod");
        return result;
    }

    public synchronized static Method resumeConv(ClassLoader loader) throws Exception {
        Class<?> conv = XposedHelpers.findClass("com.whatsapp.Conversation", loader);
        if (conv == null) throw new Exception("Conversation class not found");
        return XposedHelpers.findMethodExact(conv, "onResume");
    }

    public synchronized static Method startConv(ClassLoader loader) throws Exception {
        Class<?> conv = XposedHelpers.findClass("com.whatsapp.Conversation", loader);
        if (conv == null) throw new Exception("Conversation class not found");
        return XposedHelpers.findMethodExact(conv, "onStart");
    }

    // Media Quality

    public synchronized static Class<?> mediaQualityClass(ClassLoader loader) throws Exception {
        Class<?> result = getClazz("mediaQualityClass");
        if (result != null) return result;
        result = getIns().findClassByString(StringMatchType.Contains, loader, "videopreview/bad video");
        if (result == null) throw new Exception("mediaQuality class not found");
        saveClassPath(result, "mediaQualityClass");
        return result;
    }

    public synchronized static Method videoResolutionMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("videoResolutionMethod");
        if (result != null) return result;
        Class<?> mediaQualityClass = mediaQualityClass(loader);
        result = Arrays.stream(mediaQualityClass.getDeclaredMethods()).filter(m -> m.getParameterCount() == 3 && m.getParameterTypes()[0] == int.class && m.getParameterTypes()[1] == int.class && m.getParameterTypes()[2] == int.class && m.getReturnType() == Pair.class).findFirst().orElse(null);
        if (result == null) throw new Exception("videoQuality method not found");
        saveMethodPath(result, "videoResolutionMethod");
        return result;
    }

    public synchronized static Method videoBitrateMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("videoBitrateMethod");
        if (result != null) return result;
        Class<?> mediaQualityClass = mediaQualityClass(loader);
        result = Arrays.stream(mediaQualityClass.getDeclaredMethods()).filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == int.class && m.getReturnType() == int.class).findFirst().orElse(null);
        if (result == null) {
            result = Arrays.stream(mediaQualityClass.getDeclaredMethods()).filter(m -> m.getParameterCount() == 2 && m.getParameterTypes()[1] == int.class && m.getReturnType() == int.class).findFirst().orElse(null);
            if (result == null) throw new Exception("videoBitrate method not found");
        }
        saveMethodPath(result, "videoBitrateMethod");
        return result;
    }

    public synchronized static Method videoGifBitrateMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("videoGifBitrateMethod");
        if (result != null) return result;
        MethodDataList resultList = dexKitBridge.findMethod(new FindMethod()
                .matcher(new MethodMatcher()
                        .addUsingString("IsAnimatedGif", StringMatchType.Contains)));
        if (resultList.isEmpty()) throw new Exception("videoGifBitrate method not found");
        result = resultList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "videoGifBitrateMethod");
        return result;
    }

    public synchronized static Method imageQualityMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("imageQualityMethod");
        if (result != null) return result;
        MethodMatcher matcher = new MethodMatcher()
                .addUsingString("Unknown IntField: ", StringMatchType.Contains)
                .addUsingString("_expo_key", StringMatchType.Contains);
        MethodDataList resultList = dexKitBridge.findMethod(FindMethod.create().matcher(matcher));
        if (resultList.isEmpty()) throw new Exception("imageQuality method not found");
        result = resultList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "imageQualityMethod");
        return result;
    }

    // Props

    public synchronized static Method propsMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("propsMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader,"Unknown BooleanField");
        if (result == null) throw new Exception("propsMethod not found");
        saveMethodPath(result, "propsMethod");
        return result;
    }

    // View Once

    public synchronized static Method[] viewOnceMethods(ClassLoader loader) throws Exception {
        Method[] result = getMethods("viewOnceMethods");
        if (result != null) return result;
        ArrayList<Method> list = new ArrayList<Method>();
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod()
                .matcher(new MethodMatcher()
                        .addUsingString("unhandled view once state", StringMatchType.Contains)));
        if (methodDataList.isEmpty()) throw new Exception("unhandled view once method not found");
        Method method = methodDataList.get(0).getMethodInstance(loader);
        Class<?> clazz = null;
        if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == Context.class && method.getReturnType() == String.class) {
            clazz = method.getParameters()[1].getType();
        }
        if (clazz == null) throw new Exception("viewOnce class not found");
        Method method1 = null;
        if (clazz.getDeclaredMethods().length > 1 && clazz.getDeclaredMethods()[1].getParameterTypes()[0] == int.class) {
            method1 = clazz.getDeclaredMethods()[1];
        }
        if (method1 == null) throw new Exception("viewOnce method not found");
        MethodDataList methodDataList1 = dexKitBridge.findMethod(new FindMethod()
        .matcher(new MethodMatcher()
                .name(method1.getName())));
        if (methodDataList1.isEmpty()) throw new Exception("viewOnce methods not found");
        for (MethodData methodData : methodDataList1) {
            list.add(methodData.getMethodInstance(loader));
        }
        result = list.toArray(new Method[0]);
        saveMethodsPath(result, "viewOnceMethods");
        return result;
    }

    // Dnd Mode

    public synchronized static Method dndModeMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("dndModeMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Equals, loader, "MessageHandler/start");
        if (result == null) throw new Exception("dndModeMethod not found");
        saveMethodPath(result, "dndModeMethod");
        return result;
    }

    // Hide Receipt

    public synchronized static Method receiptMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("receiptMethod");
        if (result != null) return result;
        Class<?> deviceJidClass = XposedHelpers.findClass("com.whatsapp.jid.DeviceJid", loader);
        Method[] methods = getIns().findMethodsByString(StringMatchType.Equals, loader, "privacy_token", "false", "receipt");
        result = Arrays.stream(methods).filter(method -> method.getParameterTypes().length > 1 && method.getParameterTypes()[1] == deviceJidClass).findFirst().orElse(null);
        if (result == null) throw new Exception("receiptMethod not found");
        saveMethodPath(result, "receiptMethod");
        return result;
    }

    public synchronized static Method receiptOutsideChatMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("receiptOutsideChatMethod");
        if (result != null) return result;
        Method receiptMethod = receiptMethod(loader);
        ClassData classData = dexKitBridge.getClassData(receiptMethod.getDeclaringClass());
        if (classData == null) throw new Exception("receipt class not found");
        MethodDataList methodDataList = classData.findMethod(new FindMethod().matcher(new MethodMatcher().addUsingString("sender")));
        if (methodDataList.isEmpty()) throw new Exception("receiptOutside method not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "receiptOutsideChatMethod");
        return result;
    }

    public synchronized static Method receiptInChatMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("receiptInChatMethod");
        if (result != null) return result;
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod().matcher(new MethodMatcher().addUsingString("callCreatorJid").addUsingString("reject").addUsingNumber(0x181f)));
        if (methodDataList.isEmpty()) throw new Exception("receiptInChatMethod not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "receiptInChatMethod");
        return result;
    }

    // Separate Chats

    public synchronized static Method iconTabMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("iconTabMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader,"homeFabManager");
        if (result == null) throw new Exception("IconTab method not found");
        saveMethodPath(result, "iconTabMethod");
        return result;
    }

    public synchronized static Method tabNameMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("tabNameMethod");
        if (result != null) return result;
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod().matcher(new MethodMatcher().addUsingString("The item position should be less", StringMatchType.Contains).returnType(String.class)));
        if (methodDataList.isEmpty()) throw new Exception("tabNameMethod not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "tabNameMethod");
        return result;
    }

    public synchronized static Method tabListMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("tabListMethod");
        if (result != null) return result;
        var classData = dexKitBridge.findClass(FindClass.create().searchPackages("X.").matcher(ClassMatcher.create().addUsingString("mainContainer")));
        if (classData.isEmpty()) throw new Exception("mainContainer class not found");
        var classMain = classData.get(0).getInstance(loader);
        result = Arrays.stream(classMain.getMethods()).filter(m -> m.getName().equals("onCreate")).findFirst().orElse(null);
        if (result == null) throw new Exception("onCreate method not found");
        saveMethodPath(result, "tabListMethod");
        return result;
    }

    public synchronized static Method getTabMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("getTabMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader,"No HomeFragment mapping for community tab id:");
        if (result == null) throw new Exception("GetTab method not found");
        saveMethodPath(result, "getTabMethod");
        return result;
    }

    public synchronized static Method tabFragmentMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("tabFragmentMethod");
        if (result != null) return result;
        Class<?> clsFrag = XposedHelpers.findClass("com.whatsapp.conversationslist.ConversationsFragment", loader);
        result = Arrays.stream(clsFrag.getDeclaredMethods()).filter(m -> m.getParameterTypes().length == 0 && m.getReturnType().equals(List.class)).findFirst().orElse(null);
        if (result == null) throw new Exception("TabFragment method not found");
        saveMethodPath(result, "tabFragmentMethod");
        return result;
    }

    public synchronized static Method fabMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("fabMethod");
        if (result != null) return result;
        Class<?> cls = XposedHelpers.findClass("com.whatsapp.conversationslist.ConversationsFragment", loader);
        List<ClassData> classes = List.of(dexKitBridge.getClassData(cls));
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod().searchInClass(classes).matcher(new MethodMatcher().paramCount(0).usingNumbers(200).returnType(int.class)));
        if (methodDataList.isEmpty()) throw new Exception("Fab method not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        if (result == null) throw new Exception("Fab method not found");
        saveMethodPath(result, "fabMethod");
        return result;
    }
    
    public synchronized static Method filtersMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("filtersMethod");
        if (result != null) return result;
        var clazzFilters = getIns().findClassByString(StringMatchType.Contains, loader, "conversations/filter/performFiltering");
        if (clazzFilters == null) throw new Exception("Filters class not found");
        result = Arrays.stream(clazzFilters.getDeclaredMethods()).filter(m -> m.getName().equals("publishResults")).findFirst().orElse(null);
        if (result == null) throw new Exception("Filters method not found");
        saveMethodPath(result, "filtersMethod");
        return result;
    }

    public synchronized static Method enableCountTabMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("enableCountTabMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "Tried to set badge for invalid");
        if (result == null) throw new Exception("EnableCountTab method not found");
        saveMethodPath(result, "enableCountTabMethod");
        return result;
    }

    public synchronized static Constructor<?> enableCountTabConstructor1(ClassLoader loader) throws Exception {
        Constructor<?> result = getConstructor("enableCountTabConstructor1");
        if (result != null) return result;
        Method countMethod = enableCountTabMethod(loader);
        Class<?> indiceClass = countMethod.getParameterTypes()[1];
        ClassDataList classDataList = dexKitBridge.findClass(new FindClass().matcher(new ClassMatcher().superClass(indiceClass.getName()).addMethod(new MethodMatcher().paramCount(1))));
        if (classDataList.isEmpty()) throw new Exception("EnableCountTab method not found");
        result = classDataList.get(0).getInstance(loader).getConstructors()[0];
        saveConstructor(result, "enableCountTabConstructor1");
        return result;
    }

    public synchronized static Constructor<?> enableCountTabConstructor2(ClassLoader loader) throws Exception {
        Constructor<?> result = getConstructor("enableCountTabConstructor2");
        if (result != null) return result;
        Constructor<?> countTabConstructor1 = enableCountTabConstructor1(loader);
        Class<?> indiceClass = countTabConstructor1.getParameterTypes()[0];
        ClassDataList classDataList = dexKitBridge.findClass(new FindClass().matcher(new ClassMatcher().superClass(indiceClass.getName()).addMethod(new MethodMatcher().paramCount(1).addParamType(int.class))));
        if (classDataList.isEmpty()) throw new Exception("EnableCountTab method not found");
        result = classDataList.get(0).getInstance(loader).getConstructors()[0];
        saveConstructor(result, "enableCountTabConstructor2");
        return result;
    }

    public synchronized static Constructor<?> enableCountTabConstructor3(ClassLoader loader) throws Exception {
        Constructor<?> result = getConstructor("enableCountTabConstructor3");
        if (result != null) return result;
        Constructor<?> countTabConstructor1 = enableCountTabConstructor1(loader);
        Class<?> indiceClass = countTabConstructor1.getParameterTypes()[0];
        ClassDataList classDataList = dexKitBridge.findClass(new FindClass().matcher(new ClassMatcher().superClass(indiceClass.getName()).addMethod(new MethodMatcher().paramCount(0))));
        if (classDataList.isEmpty()) throw new Exception("EnableCountTab method not found");
        result = classDataList.get(0).getInstance(loader).getConstructors()[0];
        saveConstructor(result, "enableCountTabConstructor3");
        return result;
    }

    public synchronized static Constructor<?> recreateFragmentConstructor(ClassLoader loader) throws Exception {
        Constructor<?> result = getConstructor("recreateFragmentConstructor");
        if (result != null) return result;
        MethodDataList data = dexKitBridge.findMethod(FindMethod.create().searchPackages("X.").matcher(MethodMatcher.create().addUsingString("Instantiated fragment")));
        if (data.isEmpty()) throw new Exception("RecreateFragment method not found");
        if (!data.single().isConstructor())
            throw new Exception("RecreateFragment method not found");
        result = data.single().getConstructorInstance(loader);
        saveConstructor(result, "recreateFragmentConstructor");
        return result;
    }

    public synchronized static Field iconTabField(ClassLoader loader) throws Exception {
        Field result = getField("iconTabField");
        if (result != null) return result;
        Class<?> cls = iconTabMethod(loader).getDeclaringClass();
        Class<?> clsType = getIns().findClassByString(StringMatchType.Contains, loader, "Tried to set badge");
        result = Arrays.stream(cls.getFields()).filter(f -> f.getType().equals(clsType)).findFirst().orElse(null);
        if (result == null) {
            Class<?> cls2 = getIns().findClassByString(StringMatchType.Contains, loader, "bottom_nav_sync");
            result = Arrays.stream(cls2.getFields()).filter(f -> f.getType().equals(clsType)).findFirst().orElse(null);
            if (result == null) throw new Exception("IconTabField2 not found");
        }
        saveFieldPath(result, "iconTabField");
        return result;
    }

    public synchronized static Field iconTabLayoutField(ClassLoader loader) throws Exception {
        Field result = getField("iconTabLayoutField");
        if (result != null) return result;
        Class<?> clsType = iconTabField(loader).getType();
        Class<?> framelayout = getIns().findClassByString(StringMatchType.Contains, loader, "android:menu:presenters");
        result = Arrays.stream(clsType.getFields()).filter(f -> f.getType().equals(framelayout)).findFirst().orElse(null);
        if (result == null) throw new Exception("iconTabLayoutField not found");
        saveFieldPath(result, "iconTabLayoutField");
        return result;
    }

    public synchronized static Field iconMenuField(ClassLoader loader) throws Exception {
        Field result = getField("iconMenuField");
        if (result != null) return result;
        Class<?> clsType = iconTabLayoutField(loader).getType();
        Class<?> menuClass = getIns().findClassByString(StringMatchType.Contains, loader, "Maximum number of items");
        result = Arrays.stream(clsType.getFields()).filter(f -> f.getType().equals(menuClass)).findFirst().orElse(null);
        if (result == null) throw new Exception("iconMenuField not found");
        saveFieldPath(result, "iconMenuField");
        return result;
    }

    // Pinned Limit

    public synchronized static Method pinnedChatsMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("pinnedChatsMethod");
        if (result != null) return result;
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod().matcher(new MethodMatcher().addUsingNumber(3732).returnType(int.class)));
        if(methodDataList.isEmpty()) throw new Exception("pinnedChatsMethod not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "pinnedChatsMethod");
        return result;
    }

    public synchronized static Method pinnedHashSetMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("pinnedHashSetMethod");
        if (result != null) return result;
        Class<?> cls = getIns().findClassByString(StringMatchType.Contains, loader, "getPinnedJids/QUERY");
        if (cls == null) throw new Exception("pinnedClassList not found");
        result = Arrays.stream(cls.getDeclaredMethods()).filter(m -> m.getReturnType().equals(Set.class)).findFirst().orElse(null);
        if (result == null) throw new Exception("pinnedHashSetMethod not found");
        saveMethodPath(result, "pinnedHashSetMethod");
        return result;
    }

    public synchronized static Method pinnedLimitMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("pinnedLimitMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "count_progress");
        if (result == null) throw new Exception("pinnedLimitMethod not found");
        saveMethodPath(result, "pinnedLimitMethod");
        return result;
    }

    // Hide Read

    public synchronized static Method hideReadJobMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("hideReadJobMethod");
        if (result != null) return result;
        ClassData clsData = dexKitBridge.getClassData(XposedHelpers.findClass("com.whatsapp.jobqueue.job.SendReadReceiptJob", loader));
        MethodDataList methodDataList = clsData.findMethod(new FindMethod().matcher(new MethodMatcher().addUsingString("receipt", StringMatchType.Equals)));
        if (methodDataList.isEmpty()) {
            methodDataList = clsData.getSuperClass().findMethod(new FindMethod().matcher(new MethodMatcher().addUsingString("receipt", StringMatchType.Equals)));
        }
        if (methodDataList.isEmpty()) throw new Exception("hideReadJobMethod not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "hideReadJobMethod");
        return result;
    }

    public synchronized static Method hideViewMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("hideViewMethod");
        if (result != null) return result;
        MethodDataList methodDataList = dexKitBridge.findMethod(new FindMethod().matcher(new MethodMatcher()
                .addUsingString("privacy_token", StringMatchType.Contains)
                .addUsingString("recipient", StringMatchType.Contains)
                .addUsingString("false", StringMatchType.Contains)
                .paramCount(1,10)
        ));
        if (methodDataList.isEmpty()) throw new Exception("hideViewMethodList not found");
        result = methodDataList.get(0).getMethodInstance(loader);
        saveMethodPath(result, "hideViewMethod");
        return result;
    }

    public synchronized static Method hideViewInChatMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("hideViewInChatMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "ReadReceipts/PrivacyTokenDecisionNotComputed");
        if (result == null) throw new Exception("hideViewInChatMethod not found");
        saveMethodPath(result, "hideViewInChatMethod");
        return result;
    }

    public synchronized static Method senderPlayedMethod(ClassLoader loader) throws  Exception {
        Method result = getMethod("senderPlayedMethod");
        if (result != null) return result;
        Class<?> cls = getIns().findClassByString(StringMatchType.Contains, loader, "sendmethods/sendClearDirty");
        if (cls == null) throw new Exception("sendPlayedClass not found");
        Class<?> fMessage = FMessageClass(loader);
        result = ReferencesUtils.findMethodUsingFilter(cls, m -> m.getParameterCount() == 1 && fMessage.isAssignableFrom(m.getParameterTypes()[0]));
        if (result == null) throw new Exception("SenderPlayed method not found");
        saveMethodPath(result, "senderPlayedMethod");
        return result;
    }

    // Hide Archived Chats

    public synchronized static Method archivedHideChatsMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("archivedHideChatsMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "archive/set-content-indicator-to-empty");
        if (result == null) throw new Exception("archivedHideChatsMethod not found");
        result = result.getDeclaringClass().getMethod("setVisibility", boolean.class);
        saveMethodPath(result, "archivedHideChatsMethod");
        return result;
    }

    // Download Status

    public synchronized static Class<?> menuManagerClass(ClassLoader loader) throws Exception {
        Class<?> result = getClazz("menuManagerClass");
        if (result != null) return result;
        Method[] results = getIns().findMethodsByString(StringMatchType.Contains, loader, "MenuPopupHelper cannot be used without an anchor");
        for (var method : results) {
            if (method.getReturnType() == void.class) {
                saveClassPath(method.getDeclaringClass(), "menuManagerClass");
                return method.getDeclaringClass();
            }
        }
        throw new Exception("MenuManager class not found");
    }

    public synchronized static Method setPageActiveMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("setPageActiveMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "playbackFragment/setPageActive no-messages");
        if (result == null) throw new Exception("setPageActiveMethod not found");
        saveMethodPath(result, "setPageActiveMethod");
        return result;
    }

    public synchronized static Method  menuStatusClickMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("menuStatusClickMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "biz_block_header_chat", "Required value was null.");
        if (result == null) throw new Exception("menuStatusClickMethod not found");
        saveMethodPath(result, "menuStatusClickMethod");
        return result;
    }

    // Download View Once

    public synchronized static Method menuViewOnceManagerMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("menuViewOnceManagerMethod");
        if (result != null) return result;
        Class<?> mediaView = loader.loadClass("com.whatsapp.mediaview.MediaViewFragment");
        for(Method method : mediaView.getDeclaredMethods()) {
            if(method.getParameterCount() == 2 && method.getParameterTypes()[0] == Menu.class && method.getParameterTypes()[1] == MenuInflater.class) {
                result = method;
            }
        }
        if (result == null) throw new Exception("menuViewOnceManagerMethod not found");
        saveMethodPath(result, "menuViewOnceManagerMethod");
        return result;
    }

    public synchronized static Field menuStyleField(ClassLoader loader) throws Exception {
        Field result = getField("menuStyleField");
        if (result != null) return result;
        Method method = getIns().findMethodByString(StringMatchType.Contains, loader, "mediaViewFragment/cannot save partially");
        MethodData methodData = dexKitBridge.getMethodData(method);
        if (methodData == null) throw new Exception("menuStyleField data not found");
        for (var f : methodData.getUsingFields()) {
            Field field = f.getField().getFieldInstance(loader);
            if (field.getType().equals(int.class) && field.getDeclaringClass() == method.getDeclaringClass()) {
                result = field;
            }
        }
        if (result == null) throw new Exception("menuStyleField not found");
        saveFieldPath(result, "menuStyleField");
        return result;
    }

    // Hide Typing and Recording

    public synchronized static Method typingAndRecordingMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("typingAndRecordingMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "HandleMeComposing/sendComposing; toJid=");
        if (result == null) throw new Exception("typingAndRecordingMethod not found");
        saveMethodPath(result, "typingAndRecordingMethod");
        return result;
    }

    // Freeze Last Seen

    public synchronized static Method freezeLastSeenMethod(ClassLoader loader) throws Exception {
        Method result = getMethod("freezeLastSeenMethod");
        if (result != null) return result;
        result = getIns().findMethodByString(StringMatchType.Contains, loader, "presencestatemanager/setAvailable/new-state:");
        if (result == null) throw new Exception("freezeLastSeenMethod not found");
        saveMethodPath(result, "freezeLastSeenMethod");
        return result;
    }

    public static void start() {
        ins = new References();
    }
}
