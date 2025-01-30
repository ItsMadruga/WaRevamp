package its.madruga.warevamp.module.core;

import static its.madruga.warevamp.broadcast.receivers.WhatsAppReceiver.restartWhatsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WppCallback implements Application.ActivityLifecycleCallbacks {
    public static boolean needRestart = false;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (needRestart) {
            needRestart = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Rebooting");
            builder.setMessage("Rebooting " + activity.getPackageName());
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if (!restartWhatsapp(activity)) {
                    Toast.makeText(activity, "Unable to rebooting ", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        }
    }


    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
