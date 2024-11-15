package its.madruga.warevamp.module.hooks.others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.core.Receivers;
import its.madruga.warevamp.module.hooks.core.HooksBase;

import org.jetbrains.annotations.NotNull;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;
import static its.madruga.warevamp.module.references.ModuleResources.string.*;

public class MenuHook extends HooksBase {
    private static SharedPreferences prefs;

    public MenuHook(@NonNull @NotNull ClassLoader loader, @NonNull @NotNull XSharedPreferences preferences) {
        super(loader, preferences);
        prefs = mApp.getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        hookDndMode();
        hookRestartMenu();
    }

    private void hookRestartMenu() throws Exception {
        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Menu menu = (Menu) param.args[0];
                Activity home = (Activity) param.thisObject;
                MenuItem menuItem = menu.add(0, 0, 0, mApp.getString(reboot_wpp));
                menuItem.setOnMenuItemClickListener(menuItem1 -> {
                    Receivers.doRestart(home);
                    return true;
                });
                super.afterHookedMethod(param);
            }
        });
    }

    private void hookDndMode() throws Exception {
        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @SuppressLint("ApplySharedPref")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Menu menu = (Menu) param.args[0];
                Activity home = (Activity) param.thisObject;
                MenuItem menuItem = menu.add(0, 0, 0, mApp.getString(dnd_mode_title));
                menuItem.setOnMenuItemClickListener(menuItem1 -> {
                    boolean dndMode = prefs.getBoolean("dndMode", false);
                    if (dndMode) {
                        prefs.edit().putBoolean("dndMode", false).commit();
                        Receivers.doRestart(home);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(home);
                        builder.setTitle(mApp.getString(dnd_mode_title));
                        builder.setMessage(mApp.getString(dnd_mode_description));
                        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            prefs.edit().putBoolean("dndMode", true).commit();
                            Receivers.doRestart(home);
                        });
                        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        });
                        builder.create().show();
                    }
                    return true;
                });
                super.afterHookedMethod(param);
            }
        });
    }
}
