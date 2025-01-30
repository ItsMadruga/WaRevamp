package its.madruga.warevamp.module.core.databases.utils;

import static its.madruga.warevamp.module.hooks.core.HooksLoader.mApp;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.io.File;

public abstract class Database {
    protected SQLiteDatabase sqLiteDatabase;

    protected Database(String dbName) {
        var dataDir = mApp.getFilesDir().getParentFile();
        var dbFile = new File(dataDir, "/databases/" + dbName + ".db");
        if (!dbFile.exists()) return;
        sqLiteDatabase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
    }

    public SQLiteDatabase getDatabase() {
        return sqLiteDatabase;
    }

    public String getDatabaseName() {
        return sqLiteDatabase.getPath();
    }

    @NonNull
    @Override
    public String toString() {
        return sqLiteDatabase.getPath();
    }
}