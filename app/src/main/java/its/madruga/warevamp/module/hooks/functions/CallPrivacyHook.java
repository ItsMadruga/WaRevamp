package its.madruga.warevamp.module.hooks.functions;

import static its.madruga.warevamp.module.references.References.onCallReceivedMethod;

import android.os.Message;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.hooks.core.HooksBase;

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
                
            }
        });
    }
}
