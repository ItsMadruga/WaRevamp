package its.madruga.warevamp.module.core;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.references.ReferencesUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static its.madruga.warevamp.module.references.References.*;

public class FMessageInfo {
    private final Object messageObject;
    private static Method messageMethod;
    private static Method messageWithMediaMethod;
    private static Field keyMessage;
    private static Field mediaTypeField;
    private static Class<?> mediaMessageClass;
    private static boolean initialized;
    public static Class<?> TYPE;

    public FMessageInfo(Object message) {
        if (message == null) throw new RuntimeException("Message is null");
        this.messageObject = message;
        try {
            init(message.getClass().getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void init(ClassLoader loader) throws Exception {
        if (initialized) return;
        initialized = true;
        TYPE = FMessageClass(loader);
        keyMessage = keyMessageField(loader);
        mediaTypeField = mediaTypeField(loader);
        messageMethod = newMessageMethod(loader);
        messageWithMediaMethod = newMessageWithMediaMethod(loader);
        mediaMessageClass = mediaMessageClass(loader);
    }

    public Key getKey() {
        try {
            return new Key(keyMessage.get(messageObject));
        } catch (Exception e) {
            XposedBridge.log(e);
            return null;
        }
    }

    public String getMessageStr() {
        try {
            String message = (String) messageMethod.invoke(messageObject);
            if (message != null) return message;
            return (String) messageWithMediaMethod.invoke(messageObject);
        } catch (Exception e) {
            XposedBridge.log(e);
            return null;
        }
    }

    public boolean isMediaFile() {
        try {
            return mediaMessageClass.isInstance(messageObject);
        } catch (Exception e) {
            return false;
        }
    }

    public File getMediaFile() {
        try {
            if (!isMediaFile()) {
                return null;
            }
            for (var field : mediaMessageClass.getDeclaredFields()) {
                if (field.getType().isPrimitive()) continue;
                var fileField = ReferencesUtils.getFieldByType(field.getType(), File.class);
                if (fileField != null) {
                    var mediaFile = ReferencesUtils.getObjectField(field, messageObject);
                    return (File) fileField.get(mediaFile);
                }
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        return null;
    }

    public int getMediaType() {
        try {
            return mediaTypeField.getInt(messageObject);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        return -1;
    }

    public Object getObject() {
        return messageObject;
    }

    public static class Key {

        public final Object thisObject;
        public final String messageID;
        public final boolean isFromMe;
        public final Object remoteJid;

        public Key(Object key) {
            this.thisObject = key;
            this.messageID = (String) XposedHelpers.getObjectField(key, "A01");
            this.isFromMe = XposedHelpers.getBooleanField(key, "A02");
            this.remoteJid = XposedHelpers.getObjectField(key, "A00");
        }

    }
}
