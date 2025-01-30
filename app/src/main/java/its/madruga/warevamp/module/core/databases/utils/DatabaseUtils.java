package its.madruga.warevamp.module.core.databases.utils;

import static its.madruga.warevamp.module.references.ModuleResources.string.clean_database_ok;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

import its.madruga.warevamp.broadcast.senders.ModuleSender;

public class DatabaseUtils {

    public static void cleanDatabase(String path, Context context) {
        File dbFile = new File(path);
        if (dbFile.exists()) {
            dbFile.deleteOnExit();
            Toast.makeText(context, clean_database_ok, Toast.LENGTH_SHORT).show();
            ModuleSender.sendNeedsReboot();
        }
    }
}
