package its.madruga.warevamp.broadcast.senders;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.broadcast.Events;

public class WhatsAppSender extends EventEmitter {

    public static WhatsAppSender emitter;

    public WhatsAppSender() {
        super(mApp);
    }

    public static void start() {
        emitter = new WhatsAppSender();
    }
}
