package its.madruga.warevamp.module.hooks.functions;

import static its.madruga.warevamp.module.core.WppUtils.getContactName;
import static its.madruga.warevamp.module.core.WppUtils.getRawString;
import static its.madruga.warevamp.module.core.WppUtils.stripJID;
import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;
import static its.madruga.warevamp.module.hooks.functions.CustomPrivacyHook.getCustomPref;
import static its.madruga.warevamp.module.references.ModuleResources.string.reject_call_toast_notice;
import static its.madruga.warevamp.module.references.References.onCallReceivedMethod;

import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.WppUtils;
import its.madruga.warevamp.module.hooks.core.HooksBase;
import its.madruga.warevamp.module.references.ReferencesUtils;

public class CallPrivacyHook extends HooksBase {
    public CallPrivacyHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        XposedBridge.hookMethod(onCallReceivedMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> callInfoClass = XposedHelpers.findClass("com.whatsapp.voipcalling.CallInfo", loader);
                Object callInfo;
                if (param.args[0] instanceof Message) {
                    callInfo = ((Message) param.args[0]).obj;
                } else if (param.args.length > 1 && callInfoClass.isInstance(param.args[1])) {
                    callInfo = param.args[1];
                } else {
                    throw new Exception("Invalid call info");
                }
                if (callInfo == null || !callInfoClass.isInstance(callInfo)) return;
                if ((boolean) XposedHelpers.callMethod(callInfo, "isCaller")) return;
                Object userJid = XposedHelpers.callMethod(callInfo, "getPeerJid");
                Object callId = XposedHelpers.callMethod(callInfo, "getCallId");

                String rawJid = getRawString(userJid);

                if (checkBlock(rawJid, prefs.getString("reject_call", "disable"))) {

                    Class<?> clazzVoip = XposedHelpers.findClass("com.whatsapp.voipcalling.Voip", loader);
                    Method reject_callMethod = ReferencesUtils.findMethodUsingFilter(clazzVoip, m -> m.getName().equals("rejectCall"));
                    Method endCallMethod = ReferencesUtils.findMethodUsingFilter(clazzVoip, m -> m.getName().equals("endCall"));

                    Object voipManager = null;
                    if (!Modifier.isStatic(endCallMethod.getModifiers())) {
                        var fieldVoipManager = ReferencesUtils.findFieldUsingFilter(param.thisObject.getClass(), field -> clazzVoip.isInstance(ReferencesUtils.getObjectField(field, param.thisObject)));
                        voipManager = fieldVoipManager == null ? null : fieldVoipManager.get(param.thisObject);
                    }

                    Object[] params = ReferencesUtils.initArray(reject_callMethod.getParameterTypes());
                    params[0] = callId;
                    params[1] = "uncallable";
                    reject_callMethod.invoke(voipManager, params);
                    if (prefs.getBoolean("reject_call_toast", false)) Toast.makeText(mApp, getContactName(rawJid, false) + mApp.getString(reject_call_toast_notice), Toast.LENGTH_SHORT).show();
                    param.setResult(true);
                }
            }
        });
    }

    public boolean checkBlock(String rawJid, String type) {
        String jid = stripJID(rawJid);
        return switch (type) {
            case "all" -> true;
            case "unknown" -> {
                String name = WppUtils.getContactName(rawJid, true);
                yield name.isEmpty() || name.equals(jid) || getCustomPref(jid, "reject_call");
            }
            case "disable" -> getCustomPref(jid, "reject_call");
            default -> false;
        };
    }
}
