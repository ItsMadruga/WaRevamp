package its.madruga.warevamp.module.hooks.others;

import static its.madruga.warevamp.module.references.References.pinnedLimitMethod;

import android.view.MenuItem;

import androidx.annotation.NonNull;

import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.WppUtils;
import its.madruga.warevamp.module.hooks.core.HooksBase;

public class PinnedLimit extends HooksBase {
    public PinnedLimit(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean pinnedLimit = prefs.getBoolean("pinnedLimit", false);

        if(!pinnedLimit) return;

        XposedBridge.hookMethod(pinnedLimitMethod(loader), new XC_MethodHook() {
            private Unhook hookSize;
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                MenuItem menu = (MenuItem) param.args[0];

                int pinId = WppUtils.getResourceId("menuitem_conversations_pin", "id");

                if(menu.getItemId() == pinId) {
                    hookSize = XposedHelpers.findAndHookMethod(HashSet.class, "size", XC_MethodReplacement.returnConstant(1));
                }

            }

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (hookSize != null) hookSize.unhook();
            }
        });
    }
}
