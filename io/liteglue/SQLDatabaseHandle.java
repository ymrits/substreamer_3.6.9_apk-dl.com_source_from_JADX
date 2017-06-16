package io.liteglue;

interface SQLDatabaseHandle {
    int close();

    String getLastErrorMessage();

    long getLastInsertRowid();

    int getTotalChanges();

    boolean isOpen();

    int keyNativeString(String str);

    SQLStatementHandle newStatementHandle(String str);

    int open();
}
