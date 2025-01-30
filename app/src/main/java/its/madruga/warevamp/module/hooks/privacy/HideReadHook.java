package its.madruga.warevamp.module.hooks.privacy;

import static its.madruga.warevamp.module.core.WppUtils.getRawString;
import static its.madruga.warevamp.module.core.WppUtils.stripJID;
import static its.madruga.warevamp.module.hooks.functions.CustomPrivacyHook.getCustomPref;
import static its.madruga.warevamp.module.references.References.hideReadJobMethod;
import static its.madruga.warevamp.module.references.References.hideViewInChatMethod;
import static its.madruga.warevamp.module.references.References.hideViewMethod;
import static its.madruga.warevamp.module.references.References.senderPlayedMethod;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.FMessageInfo;
import its.madruga.warevamp.module.hooks.core.HooksBase;
import its.madruga.warevamp.module.references.ReferencesUtils;

public class HideReadHook extends HooksBase {
    public HideReadHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean hideread = prefs.getBoolean("hide_read", false);


        Class<?> sendJob = XposedHelpers.findClass("com.whatsapp.jobqueue.job.SendReadReceiptJob", loader);

        XposedBridge.hookMethod(hideReadJobMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (hideread || getCustomPref(stripJID((String) param.method.getDeclaringClass().getDeclaredField("jid").get(param.thisObject)), "hide_read")) {
                    if (!sendJob.isInstance(param.thisObject)) return;
                    param.setResult(null);
                }
            }
        });

        XposedBridge.hookMethod(hideViewMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String stripJID = stripJID(getRawString(param.args[1]));
                if (stripJID.contains(".")) stripJID = stripJID.substring(0, stripJID.indexOf("."));
                if (hideread || getCustomPref(stripJID, "hide_read")) {
                    if (!ReferencesUtils.isCalledFromMethod(hideViewInChatMethod(loader))) return;
                    var p4 = param.args[4];
                    if (p4 != null && p4.equals("read")) {
                        param.args[4] = null;
                        super.beforeHookedMethod(param);
                    }
                }
            }
        });

        XposedBridge.hookMethod(senderPlayedMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String jid = stripJID(getRawString(new FMessageInfo(param.args[0]).getKey().remoteJid));
                if (hideread || getCustomPref(jid, "hide_read")) {
                    param.setResult(null);
                }
            }
        });
    }
}
