package its.madruga.warevamp.module.hooks.privacy;

import static its.madruga.warevamp.module.core.WppUtils.getRawString;
import static its.madruga.warevamp.module.core.WppUtils.stripJID;
import static its.madruga.warevamp.module.hooks.functions.CustomPrivacyHook.getCustomPref;
import static its.madruga.warevamp.module.references.References.typingAndRecordingMethod;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.module.hooks.core.HooksBase;

public class HideTypingRecordingHook extends HooksBase {
    public HideTypingRecordingHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean hide_typing = prefs.getBoolean("hide_typing", false);
        boolean hide_recording = prefs.getBoolean("hide_recording", false);

        XposedBridge.hookMethod(typingAndRecordingMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String stripJID = stripJID(getRawString(param.args[1]));
                var p1 = (int) param.args[2];
                if (p1 == 1 && (hide_recording || getCustomPref(stripJID, "hide_recording"))) {
                    param.setResult(null);
                    return;
                }
                if (p1 == 0 && (hide_typing || getCustomPref(stripJID, "hide_typing"))) {
                    param.setResult(null);
                    return;
                }
                super.beforeHookedMethod(param);
            }
        });
    }
}
