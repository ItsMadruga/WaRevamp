package its.madruga.warevamp.broadcast.receivers;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.home;
import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import de.robv.android.xposed.XposedBridge;
import its.madruga.warevamp.broadcast.Events;
import its.madruga.warevamp.broadcast.senders.WhatsAppSender;
import its.madruga.warevamp.module.core.WppCallback;
import its.madruga.warevamp.module.core.databases.AxolotlDatabase;
import its.madruga.warevamp.module.core.databases.MsgstoreDatabase;
import its.madruga.warevamp.module.core.databases.StickerDatabase;
import its.madruga.warevamp.module.core.databases.WaDatabase;
import its.madruga.warevamp.module.core.databases.utils.DatabaseUtils;

public class WhatsAppReceiver extends EventReceiver {
    public WhatsAppReceiver() {
        super(mApp);
    }

    public static void start() {
        new WhatsAppReceiver().registerAllReceivers();
    }

    @Override
    public void registerAllReceivers() {
        registerReceiver(Events.ACTION_WA_REVAMP_CHECK, onModuleStatusCheck());
        registerReceiver(Events.ACTION_WA_REVAMP_CLEAN_DATABASE, onCleanDatabase());
        registerReceiver(Events.ACTION_WA_REVAMP_NEED_REBOOT, onRebootNeeded());
        registerReceiver(Events.ACTION_WA_REVAMP_REBOOT, onReboot());
    }

    private BroadcastReceiver onModuleStatusCheck() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                var enabledIntent = new Intent(Events.ACTION_WA_REVAMP_ENABLED);
                try {
                    enabledIntent.putExtra("enabled", true);
                    enabledIntent.putExtra("version", mApp.getPackageManager().getPackageInfo(mApp.getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                WhatsAppSender.emitter.emit(enabledIntent);
            }
        };
    }

    private BroadcastReceiver onRebootNeeded() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WppCallback.needRestart = true;
            }
        };
    }

    public BroadcastReceiver onReboot() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CharSequence appName = mApp.getPackageManager().getApplicationLabel(mApp.getApplicationInfo());
                Toast.makeText(mApp, "Rebooting " + appName + "...", Toast.LENGTH_SHORT).show();

                if (!restartWhatsapp())
                    Toast.makeText(mApp, "Unable to rebooting " + appName, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public BroadcastReceiver onCleanDatabase() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String dbName = intent.getStringExtra("dbName");
                if (dbName == null) return;
                switch (dbName) {
                    case "msgstore":
                        DatabaseUtils.cleanDatabase(MsgstoreDatabase.getInstance().getDatabase().getPath(), context);
                        break;
                    case "sticker":
                        DatabaseUtils.cleanDatabase(StickerDatabase.getInstance().getDatabase().getPath(), context);
                        break;
                    case "wa":
                        DatabaseUtils.cleanDatabase(WaDatabase.getInstance().getDatabase().getPath(), context);
                        break;
                    case "axolotl":
                        DatabaseUtils.cleanDatabase(AxolotlDatabase.getInstance().getDatabase().getPath(), context);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public static boolean restartWhatsapp(Activity... activity) {
        var targetActivity = activity.length > 0 ? activity[0] : home;
        Intent intent = mApp.getPackageManager().getLaunchIntentForPackage(mApp.getPackageName());
        if (mApp != null && targetActivity != null) {
            targetActivity.finishAffinity();
            mApp.startActivity(intent);
        }
        Runtime.getRuntime().exit(0);
        return true;
    }
}
