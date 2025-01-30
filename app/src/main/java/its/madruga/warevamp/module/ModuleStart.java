package its.madruga.warevamp.module;

import android.content.res.XModuleResources;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.module.hooks.core.HooksLoader;
import its.madruga.warevamp.module.hooks.media.DisableFlagSecureHook;
import its.madruga.warevamp.module.references.ModuleResources;
import its.madruga.warevamp.app.core.Utils;
import its.madruga.warevamp.app.core.XposedChecker;

public class ModuleStart implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    private static XSharedPreferences pref;
    private static String PATH;

    @NonNull
    public static XSharedPreferences getPref() {
        if (pref == null) {
            pref = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            pref.makeWorldReadable();
            pref.reload();
        }
        return pref;
    }
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        ClassLoader classLoader = lpparam.classLoader;
        String sourcePath = lpparam.appInfo.sourceDir;
        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedChecker.setActiveModule(lpparam.classLoader);
        }

        if (packageName.equals(Utils.WHATSAPP_PACKAGE) || packageName.equals(Utils.WHATSAPP_WEB_PACKAGE)) {
            HooksLoader.initialize(getPref(), classLoader, sourcePath);
        }

        DisableFlagSecureHook.doHook(lpparam, getPref());

    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        String packageName = resparam.packageName;

        if (packageName.equals(BuildConfig.APPLICATION_ID))
            getPref();

        if (!packageName.equals(Utils.WHATSAPP_PACKAGE) && !packageName.equals(Utils.WHATSAPP_WEB_PACKAGE)) return;

        XModuleResources res = XModuleResources.createInstance(PATH, resparam.res);

        for (Field field : ModuleResources.string.class.getFields()) {
            int resId = res.getIdentifier(field.getName(), "string", BuildConfig.APPLICATION_ID);
            int addedResId = resparam.res.addResource(res, resId);
           field.set(null, addedResId);
        }

        for (Field field : ModuleResources.array.class.getFields()) {
            int resId = res.getIdentifier(field.getName(), "array", BuildConfig.APPLICATION_ID);
            int addedResId = resparam.res.addResource(res, resId);
            field.set(null, addedResId);
        }

        for (Field field : ModuleResources.drawable.class.getFields()) {
            int resId = res.getIdentifier(field.getName(), "drawable", BuildConfig.APPLICATION_ID);
            int addedResId = resparam.res.addResource(res, resId);
            field.set(null, addedResId);
        }

        for (Field field : ModuleResources.layout.class.getFields()) {
            int resId = res.getIdentifier(field.getName(), "layout", BuildConfig.APPLICATION_ID);
            int addedResId = resparam.res.addResource(res, resId);
            field.set(null, addedResId);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        PATH = startupParam.modulePath;
        XposedBridge.log("WaRevamp InitZygote");
    }
}
