package io.liteglue;

import java.sql.SQLException;

public interface SQLiteStatement {
    void bindDouble(int i, double d) throws SQLException;

    void bindInteger(int i, int i2) throws SQLException;

    void bindLong(int i, long j) throws SQLException;

    void bindNull(int i) throws SQLException;

    void bindTextNativeString(int i, String str) throws SQLException;

    void dispose() throws SQLException;

    int getColumnCount() throws SQLException;

    double getColumnDouble(int i) throws SQLException;

    int getColumnInteger(int i) throws SQLException;

    long getColumnLong(int i) throws SQLException;

    String getColumnName(int i) throws SQLException;

    String getColumnTextNativeString(int i) throws SQLException;

    int getColumnType(int i) throws SQLException;

    boolean step() throws SQLException;
}
