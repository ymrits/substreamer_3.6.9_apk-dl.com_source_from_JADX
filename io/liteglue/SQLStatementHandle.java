package io.liteglue;

interface SQLStatementHandle {
    int bindDouble(int i, double d);

    int bindInteger(int i, int i2);

    int bindLong(int i, long j);

    int bindNull(int i);

    int bindTextNativeString(int i, String str);

    int finish();

    int getColumnCount();

    double getColumnDouble(int i);

    int getColumnInteger(int i);

    long getColumnLong(int i);

    String getColumnName(int i);

    String getColumnTextNativeString(int i);

    int getColumnType(int i);

    int prepare();

    int step();
}
