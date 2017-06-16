package io.liteglue;

import java.sql.SQLException;

public interface SQLiteConnectionFactory {
    SQLiteConnection newSQLiteConnection(String str, int i) throws SQLException;
}
