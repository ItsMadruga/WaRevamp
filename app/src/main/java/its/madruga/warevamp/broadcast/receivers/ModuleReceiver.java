package its.madruga.warevamp.broadcast.receivers;

import android.util.Log;

import its.madruga.warevamp.App;

public class ModuleReceiver extends EventReceiver {
    public ModuleReceiver() {
        super(App.getInstance());
    }

    public static void start() {
        new ModuleReceiver().registerAllReceivers();
    }

    @Override
    public void registerAllReceivers() {
    }
}
