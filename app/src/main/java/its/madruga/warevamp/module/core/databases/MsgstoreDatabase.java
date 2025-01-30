package its.madruga.warevamp.module.core.databases;

import its.madruga.warevamp.module.core.databases.utils.Database;

public class MsgstoreDatabase extends Database {

    private static MsgstoreDatabase mInstance;

    private MsgstoreDatabase() {
        super("msgstore");
    }

    public static MsgstoreDatabase getInstance() {
        synchronized (MsgstoreDatabase.class) {
            if (mInstance == null || mInstance.sqLiteDatabase == null || !mInstance.sqLiteDatabase.isOpen()) {
                mInstance = new MsgstoreDatabase();
            }
        }
        return mInstance;
    }
}