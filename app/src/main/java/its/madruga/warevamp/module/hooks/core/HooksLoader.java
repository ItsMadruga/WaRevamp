package its.madruga.warevamp.module.hooks.core;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Instrumentation;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.broadcast.receivers.WhatsAppReceiver;
import its.madruga.warevamp.broadcast.senders.WhatsAppSender;
import its.madruga.warevamp.module.core.WppCallback;
import its.madruga.warevamp.module.hooks.customization.HideArchivedChatsHook;
import its.madruga.warevamp.module.hooks.media.DownloadStatusHook;
import its.madruga.warevamp.module.hooks.media.DownloadViewOnceHook;
import its.madruga.warevamp.module.hooks.functions.CustomPrivacyHook;
import its.madruga.warevamp.module.hooks.others.PinnedLimit;
import its.madruga.warevamp.module.hooks.customization.SeparateGroupsHook;
import its.madruga.warevamp.module.hooks.others.MenuHook;
import its.madruga.warevamp.module.hooks.privacy.DndModeHook;
import its.madruga.warevamp.module.hooks.privacy.FreezeLastSeenHook;
import its.madruga.warevamp.module.hooks.privacy.HideReadHook;
import its.madruga.warevamp.module.hooks.privacy.HideReceiptHook;
import its.madruga.warevamp.module.hooks.privacy.HideTypingRecordingHook;
import its.madruga.warevamp.module.references.References;
import its.madruga.warevamp.module.hooks.media.MediaQualityHook;
import its.madruga.warevamp.module.hooks.others.OthersHook;
import its.madruga.warevamp.module.hooks.functions.AntiRevokeHook;
import its.madruga.warevamp.module.hooks.functions.AntiViewOnceHook;
import its.madruga.warevamp.module.references.ReferencesCache;

public class HooksLoader {
    public static Application mApp;
    @SuppressLint("StaticFieldLeak")
    public static Activity home;
    public static ArrayList<String> list = new ArrayList<>();

    public static void initialize(XSharedPreferences pref, ClassLoader loader, String sourceDir) {

        XposedBridge.log("Starting WhatsApp Broadcasts");
        if (!References.initDexKit(sourceDir)) {
            XposedBridge.log("Unable to start DexKit");
            return;
        } else {
            XposedBridge.log("DexKit Init");
        }

        XposedBridge.log("Starting WhatsApp Broadcasts");
        XposedHelpers.findAndHookMethod(Instrumentation.class, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Exception {
                mApp = (Application) param.args[0];
                mApp.registerActivityLifecycleCallbacks(new WppCallback());
                ReferencesCache.init(mApp, loader);
                References.start();
                plugins(loader, pref);
                // Initializing WhatsApp Broadcasts
                XposedBridge.log("Starting WhatsApp Broadcasts");
                WhatsAppSender.start();
                WhatsAppReceiver.start();
            }
        });

        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                home = (Activity) param.thisObject;

                if (!list.isEmpty()) {
                    new AlertDialog.Builder(home)
                            .setTitle("Erro detectado")
                            .setMessage("As seguintes opcoes estao com erro:\n\n" + String.join("\n", list.toArray(new String[0])))
                            .show();
                }
            }
        });
    }

    private static void plugins(@NonNull ClassLoader loader, @NonNull XSharedPreferences pref) {
        ArrayList<String> loadedClasses = new ArrayList<>();
        var classes = new Class<?>[]{
                AntiRevokeHook.class,
                AntiViewOnceHook.class,
                MediaQualityHook.class,
                OthersHook.class,
                MenuHook.class,
                DndModeHook.class,
                HideReceiptHook.class,
                SeparateGroupsHook.class,
                PinnedLimit.class,
                HideReadHook.class,
                HideArchivedChatsHook.class,
                DownloadStatusHook.class,
                DownloadViewOnceHook.class,
                HideTypingRecordingHook.class,
                FreezeLastSeenHook.class,
                CustomPrivacyHook.class
        };

        for (var c : classes) {
            try {
                var constructor = c.getConstructor(ClassLoader.class, XSharedPreferences.class);
                var plugin = constructor.newInstance(loader, pref);
                var method = c.getMethod("doHook");
                method.invoke(plugin);
                loadedClasses.add("-> "  + c.getName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     InstantiationException e) {
                XposedBridge.log(e);
                if (e instanceof InvocationTargetException) {
                    list.add(c.getSimpleName());
                }
            }
        }

        XposedBridge.log("Loaded Class:\n\n" + String.join("\n", loadedClasses.toArray(new String[0])));
    }
}
