package its.madruga.warevamp.module.core;

import android.database.Cursor;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.warevamp.module.core.databases.WaDatabase;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;

public class WppUtils {

    public static String stripJID(String str) {
        try {
            return (str.contains("@g.us") || str.contains("@s.whatsapp.net") || str.contains("@broadcast")) ? str.substring(0, str.indexOf("@")) : str;
        } catch (Exception e) {
            XposedBridge.log(e.getMessage());
            return str;
        }
    }

    public static String getRawString(Object objJid) {
        if (objJid == null) return "";
        else return (String) XposedHelpers.callMethod(objJid, "getRawString");
    }

    public static String[] StringToStringArray(String str) {
        try {
            return str.substring(1, str.length() - 1).replaceAll("\\s", "").split(",");
        } catch (Exception unused) {
            return null;
        }
    }

    public static int getResourceId(String name, String type) {
        int id = mApp.getResources().getIdentifier(name, type, mApp.getPackageName());
        if (id == 0) {
           Log.e("WaRevamp", "Resource not found: " + name);
        }
        return id;
    }

    public static String getContactName(String id, boolean save) {
        WaDatabase db = WaDatabase.getInstance();
        String name = null;
        String queryString;
        if (save) {
            queryString = "jid = ? AND raw_contact_id > 0";
        } else {
            queryString = "jid = ?";
        }

        Cursor cursor = db.getDatabase().query("wa_contacts", new String[]{"display_name"}, queryString, new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(0);
            cursor.close();
        }
        return name == null ? "" : name;
    }

}
