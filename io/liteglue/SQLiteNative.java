package io.liteglue;

public class SQLiteNative {
    public static final int SQLC_API_VERSION = 1;
    public static final int SQLC_BLOB = 4;
    public static final int SQLC_FLOAT = 2;
    public static final int SQLC_INTEGER = 1;
    public static final int SQLC_NULL = 5;
    public static final int SQLC_OPEN_CREATE = 4;
    public static final int SQLC_OPEN_FULLMUTEX = 65536;
    public static final int SQLC_OPEN_MEMORY = 128;
    public static final int SQLC_OPEN_NOMUTEX = 32768;
    public static final int SQLC_OPEN_PRIVATECACHE = 262144;
    public static final int SQLC_OPEN_READONLY = 1;
    public static final int SQLC_OPEN_READWRITE = 2;
    public static final int SQLC_OPEN_SHAREDCACHE = 131072;
    public static final int SQLC_OPEN_URI = 64;
    public static final int SQLC_RESULT_ABORT = 4;
    public static final int SQLC_RESULT_CONSTRAINT = 19;
    public static final int SQLC_RESULT_DONE = 101;
    public static final int SQLC_RESULT_ERROR = 1;
    public static final int SQLC_RESULT_INTERNAL = 2;
    public static final int SQLC_RESULT_MISMATCH = 20;
    public static final int SQLC_RESULT_MISUSE = 21;
    public static final int SQLC_RESULT_OK = 0;
    public static final int SQLC_RESULT_PERM = 3;
    public static final int SQLC_RESULT_ROW = 100;
    public static final int SQLC_TEXT = 3;

    public static native long sqlc_api_db_open(int i, String str, int i2);

    public static native int sqlc_api_version_check(int i);

    public static native int sqlc_db_close(long j);

    public static native int sqlc_db_errcode(long j);

    public static native String sqlc_db_errmsg_native(long j);

    public static native int sqlc_db_key_native_string(long j, String str);

    public static native long sqlc_db_last_insert_rowid(long j);

    public static native long sqlc_db_open(String str, int i);

    public static native long sqlc_db_prepare_st(long j, String str);

    public static native int sqlc_db_total_changes(long j);

    public static native String sqlc_errstr_native(int i);

    public static native int sqlc_st_bind_double(long j, int i, double d);

    public static native int sqlc_st_bind_int(long j, int i, int i2);

    public static native int sqlc_st_bind_long(long j, int i, long j2);

    public static native int sqlc_st_bind_null(long j, int i);

    public static native int sqlc_st_bind_text_native(long j, int i, String str);

    public static native int sqlc_st_column_count(long j);

    public static native double sqlc_st_column_double(long j, int i);

    public static native int sqlc_st_column_int(long j, int i);

    public static native long sqlc_st_column_long(long j, int i);

    public static native String sqlc_st_column_name(long j, int i);

    public static native String sqlc_st_column_text_native(long j, int i);

    public static native int sqlc_st_column_type(long j, int i);

    public static native int sqlc_st_finish(long j);

    public static native int sqlc_st_step(long j);
}
