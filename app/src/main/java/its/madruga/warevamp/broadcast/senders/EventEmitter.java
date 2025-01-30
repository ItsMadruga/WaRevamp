package its.madruga.warevamp.broadcast.senders;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class EventEmitter {
    private final Application application;
    public EventEmitter emitter;
    public static final String TAG = "EventEmitter";

    public EventEmitter(Application application) {
        this.application = application;
        emitter = this;
    }

    public void emit(Intent intent) {
        application.sendBroadcast(intent);
    }
    public void emit(String event) {
        var intent = new Intent(event);
        emit(intent);
    }
}
