package its.madruga.warevamp.module.core.databases;

import its.madruga.warevamp.module.core.databases.utils.Database;

public class WaDatabase extends Database {

    private static WaDatabase mInstance;

    private WaDatabase() {
        super("wa");
    }

    public static WaDatabase getInstance() {
        synchronized (WaDatabase.class) {
            if (mInstance == null || mInstance.sqLiteDatabase == null || !mInstance.sqLiteDatabase.isOpen()) {
                mInstance = new WaDatabase();
            }
        }
        return mInstance;
    }
}