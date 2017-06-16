package io.liteglue;

import java.sql.SQLException;

public interface SQLiteConnection {
    void dispose() throws SQLException;

    long getLastInsertRowid() throws SQLException;

    int getTotalChanges() throws SQLException;

    void keyNativeString(String str) throws SQLException;

    SQLiteStatement prepareStatement(String str) throws SQLException;
}
