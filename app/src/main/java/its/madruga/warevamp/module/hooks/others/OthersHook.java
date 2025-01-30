package its.madruga.warevamp.module.hooks.others;

import androidx.annotation.NonNull;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.module.hooks.core.HooksBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static its.madruga.warevamp.module.references.References.propsMethod;

public class OthersHook extends HooksBase {
    HashMap<Integer, Boolean> propList;

    public OthersHook(@NonNull @NotNull ClassLoader loader, @NonNull @NotNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();



        propList = new HashMap<>();

        // Transcrição
        propList.put(8632, true);
        propList.put(2890, true);
        propList.put(9215, true);

        // Novo menu
        propList.put(2889, prefs.getBoolean("novoMenu", false));

        hookProps();
    }

    private void hookProps() throws Exception {
        XposedBridge.hookMethod(propsMethod(loader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int i = (int) param.args[param.args.length - 1];

                if (propList.containsKey(i)) {
                    param.setResult(propList.get(i));
                }
            }
        });
    }
}
