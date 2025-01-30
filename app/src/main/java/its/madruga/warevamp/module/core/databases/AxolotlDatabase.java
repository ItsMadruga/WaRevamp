package its.madruga.warevamp.module.core.databases;

import its.madruga.warevamp.module.core.databases.utils.Database;

public class AxolotlDatabase extends Database {

    private static AxolotlDatabase mInstance;

    private AxolotlDatabase() {
        super("axolotl");
    }

    public static AxolotlDatabase getInstance() {
        synchronized (AxolotlDatabase.class) {
            if (mInstance == null || mInstance.sqLiteDatabase == null || !mInstance.sqLiteDatabase.isOpen()) {
                mInstance = new AxolotlDatabase();
            }
        }
        return mInstance;
    }
}