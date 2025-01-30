package its.madruga.warevamp.module.hooks.customization;

import static its.madruga.warevamp.module.references.References.archivedHideChatsMethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.app.core.Utils;
import its.madruga.warevamp.module.core.WppUtils;
import its.madruga.warevamp.module.hooks.core.HooksBase;

public class HideArchivedChatsHook extends HooksBase {
    public HideArchivedChatsHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        boolean hide_archived = prefs.getBoolean("hide_archived", false);

        if (!hide_archived) return;

        XposedBridge.hookMethod(archivedHideChatsMethod(loader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                param.args[0] = false;
            }
        });

        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Activity homeActivity = (Activity) param.thisObject;
                View toolbar = homeActivity.findViewById(WppUtils.getResourceId("toolbar", "id"));
                toolbar.setOnLongClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClassName(Utils.WHATSAPP_PACKAGE, "com.whatsapp.conversationslist.ArchivedConversationsActivity");
                    homeActivity.startActivity(intent);
                    return true;
                });
            }
        });


    }
}
