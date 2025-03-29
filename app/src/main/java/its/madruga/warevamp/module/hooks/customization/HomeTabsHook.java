package its.madruga.warevamp.module.hooks.customization;

import static its.madruga.warevamp.module.references.References.homeActivityClass;
import static its.madruga.warevamp.module.references.References.tabListMethod;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.module.hooks.core.HooksBase;

public class HomeTabsHook extends HooksBase {

    public HomeTabsHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        hookTabList();
    }

    public void hookTabList() throws Exception {
        Class<?> homeActivity = homeActivityClass(loader);
        Field tabList = Arrays.stream(homeActivity.getDeclaredFields()).filter(f -> f.getType().equals(List.class)).findFirst().orElse(null);
        if (tabList == null) throw new Exception("tab list is null!");

        tabList.setAccessible(true);

        XposedBridge.hookMethod(tabListMethod(loader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                log(tabList.get(null).toString());
            }
        });
    }
}
