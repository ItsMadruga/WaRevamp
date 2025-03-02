package its.madruga.warevamp;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.List;

import its.madruga.warevamp.broadcast.receivers.ModuleReceiver;
import its.madruga.warevamp.broadcast.senders.ModuleSender;

public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DynamicColors.applyToActivitiesIfAvailable(this);
        antiMinifyString();
        ModuleSender.start();
        ModuleReceiver.start();
    }

    public void antiMinifyString() {
        List<Integer> list = new ArrayList<>();
        list.add(R.string.download_status);
        list.add(R.string.message_deleted);
        list.add(R.string.reboot_wpp);
        list.add(R.string.dnd_mode_title);
        list.add(R.string.dnd_mode_description);
        list.add(R.string.clean_database_ok);
        list.add(R.string.custom_privacy);
        list.add(R.string.tab_groups);
        list.add(R.string.download_viewonce);
        list.add(R.string.reject_call_toast_notice);
        list.add(R.array.custom_priv_entries);
        list.add(R.array.custom_priv_values);
        list.add(R.drawable.download_icon);
        list.add(R.drawable.twotone_auto_awesome_24);
        list.add(R.drawable.round_wifi_24);
        list.add(R.drawable.wifi_off_24px);
    }

    public static App getInstance() {
        return instance;
    }
}
