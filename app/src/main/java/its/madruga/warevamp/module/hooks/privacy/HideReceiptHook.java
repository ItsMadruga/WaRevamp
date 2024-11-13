package its.madruga.warevamp.module.hooks.privacy;

import androidx.annotation.NonNull;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.module.hooks.core.HooksBase;
import org.jetbrains.annotations.NotNull;

import static its.madruga.warevamp.module.references.References.*;
import static its.madruga.warevamp.module.references.ReferencesUtils.isCalledFromMethod;

public class HideReceiptHook extends HooksBase {
    public HideReceiptHook(@NonNull @NotNull ClassLoader loader, @NonNull @NotNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean hideReceipt = prefs.getBoolean("hideReceipt", false);
        if (!hideReceipt) return;

        XposedBridge.hookMethod(receiptMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!isCalledFromMethod(receiptOutsideChatMethod(loader)) && !isCalledFromMethod(receiptInChatMethod(loader)))
                    return;
                if (param.args[4] != "sender") {
                    param.args[4] = "inactive";
                }
            }
        });
    }
}