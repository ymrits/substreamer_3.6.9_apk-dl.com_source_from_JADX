package io.liteglue;

import java.sql.SQLException;

public class SQLiteConnector implements SQLiteConnectionFactory {
    static boolean isLibLoaded;

    static {
        isLibLoaded = false;
    }

    public SQLiteConnector() {
        if (!isLibLoaded) {
            System.loadLibrary("sqlc-native-driver");
            if (SQLiteNative.sqlc_api_version_check(1) != 0) {
                throw new RuntimeException("native library version mismatch");
            }
            isLibLoaded = true;
        }
    }

    public SQLiteConnection newSQLiteConnection(String str, int i) throws SQLException {
        return new SQLiteGlueConnection(str, i);
    }
}
