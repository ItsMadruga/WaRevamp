package its.madruga.warevamp.broadcast.receivers;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import java.util.HashMap;

public abstract class EventReceiver {
    public Application application;
    public HashMap<String, BroadcastReceiver> listeners = new HashMap<>();

    public static final String TAG = "EventReceiver";
    public EventReceiver(Application application) {
        this.application = application;
        registerAllReceivers();
    }

    public void registerReceiver(String action, BroadcastReceiver listener) {
        ContextCompat.registerReceiver(application, listener, new IntentFilter(action), ContextCompat.RECEIVER_EXPORTED);
    }

    public abstract void registerAllReceivers();
}
