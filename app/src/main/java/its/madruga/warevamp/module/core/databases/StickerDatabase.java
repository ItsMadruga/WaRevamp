package its.madruga.warevamp.module.core.databases;

import its.madruga.warevamp.module.core.databases.utils.Database;

public class StickerDatabase extends Database {

    private static StickerDatabase mInstance;

    private StickerDatabase() {
        super("stickers");
    }

    public static StickerDatabase getInstance() {
        synchronized (StickerDatabase.class) {
            if (mInstance == null || mInstance.sqLiteDatabase == null || !mInstance.sqLiteDatabase.isOpen()) {
                mInstance = new StickerDatabase();
            }
        }
        return mInstance;
    }
}