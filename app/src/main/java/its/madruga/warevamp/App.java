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
        list.add(R.drawable.download_icon);
    }

    public static App getInstance() {
        return instance;
    }
}
