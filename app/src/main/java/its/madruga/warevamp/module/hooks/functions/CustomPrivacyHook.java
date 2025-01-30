package its.madruga.warevamp.module.hooks.functions;

import static android.content.Context.MODE_PRIVATE;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;
import static its.madruga.warevamp.module.references.ModuleResources.array.custom_priv_entries;
import static its.madruga.warevamp.module.references.ModuleResources.array.custom_priv_values;
import static its.madruga.warevamp.module.references.ModuleResources.drawable.twotone_auto_awesome_24;
import static its.madruga.warevamp.module.references.ModuleResources.string.custom_privacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.module.core.WppUtils;
import its.madruga.warevamp.module.hooks.core.HooksBase;

public class CustomPrivacyHook extends HooksBase {

    private static SharedPreferences cPref;
    private static String jid;
    private static String gid;
    public CustomPrivacyHook(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
        cPref = mApp.getSharedPreferences(BuildConfig.APPLICATION_ID + "_custom_preferences", MODE_PRIVATE);
    }

    @Override
    public void doHook() throws Exception {
        super.doHook();

        Class<?> chatInfo = XposedHelpers.findClass("com.whatsapp.chatinfo.ContactInfoActivity", loader);
        Class<?> groupInfo = XposedHelpers.findClass("com.whatsapp.group.GroupChatInfoActivity", loader);


        XposedHelpers.findAndHookMethod(chatInfo, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Activity activity = (Activity) param.thisObject;

                Intent intent = activity.getIntent();
                if (intent.hasExtra("jid")) {
                    jid = WppUtils.stripJID(intent.getStringExtra("jid"));
                    gid = null;
                }

                injectCustomPrefernece(activity);
            }
        });

        XposedHelpers.findAndHookMethod(groupInfo, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Activity activity = (Activity) param.thisObject;

                Intent intent = activity.getIntent();
                if (intent.hasExtra("gid")) {
                    gid = WppUtils.stripJID(intent.getStringExtra("gid"));
                    jid = null;
                }

                injectCustomPrefernece(activity);

            }
        });
    }

    private static MultiSelectListPreference createList(Context context, String id) {
        MultiSelectListPreference mList = new MultiSelectListPreference(context);

        String[] entries = mApp.getResources().getStringArray(custom_priv_entries);
        String[] values = mApp.getResources().getStringArray(custom_priv_values);

        mList.setEntries(entries);
        mList.setEntryValues(values);
        mList.setTitle(mApp.getString(custom_privacy));
        mList.setIcon(mApp.getDrawable(WppUtils.getResourceId("settings_security", "drawable")));
        mList.setDialogTitle(mApp.getString(custom_privacy));
        mList.setKey(id);
        return mList;
    }

    private static void injectCustomPrefernece(Activity activity) {
        if (activity == null) return;

        LinearLayout contactInfoCard = activity.findViewById(WppUtils.getResourceId("contact_info_security_card_layout", "id"));
        if (contactInfoCard == null) return;

        FrameLayout containerView = new FrameLayout(activity);
        containerView.setId(WppUtils.getResourceId("customPanel", "id"));
        containerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        contactInfoCard.addView(containerView, 0);

        activity.getFragmentManager().beginTransaction().replace(WppUtils.getResourceId("customPanel", "id"), new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());

            getPreferenceManager().setSharedPreferencesName(BuildConfig.APPLICATION_ID + "_custom_preferences");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            String id = jid != null ? jid : gid;

            preferenceScreen.addPreference(createList(getActivity(), id));
            setPreferenceScreen(preferenceScreen);

            findPreference(id).setOnPreferenceChangeListener((preference, o) -> true);
        }
    }

    public static boolean getCustomPref(String id, String key) {
        if (id == null || key == null) return false;
        Set<String> set = cPref.getStringSet(id, null);
        return set != null && set.contains(key);
    }
}
