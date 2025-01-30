package its.madruga.warevamp.module.hooks.privacy;

import static its.madruga.warevamp.module.references.References.freezeLastSeenMethod;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.module.hooks.core.HooksBase;

public class FreezeLastSeenHook extends HooksBase {
    public FreezeLastSeenHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean freeze = prefs.getBoolean("freeze_last_seen", false);

        if (!freeze) return;

        XposedBridge.hookMethod(freezeLastSeenMethod(loader), XC_MethodReplacement.DO_NOTHING);
    }
}
