package io.liteglue;

class SQLGDatabaseHandle implements SQLDatabaseHandle {
    String dbfilename;
    private long dbhandle;
    int openflags;

    private class SQLGStatementHandle implements SQLStatementHandle {
        String sql;
        private long sthandle;

        private SQLGStatementHandle(String str) {
            this.sql = null;
            this.sthandle = 0;
            this.sql = str;
        }

        public int prepare() {
            if (this.sql == null || this.sthandle != 0) {
                return 21;
            }
            long sqlc_db_prepare_st = SQLiteNative.sqlc_db_prepare_st(SQLGDatabaseHandle.this.dbhandle, this.sql);
            if (sqlc_db_prepare_st < 0) {
                return (int) (-sqlc_db_prepare_st);
            }
            this.sthandle = sqlc_db_prepare_st;
            return 0;
        }

        public int bindDouble(int i, double d) {
            if (this.sthandle == 0) {
                return 21;
            }
            return SQLiteNative.sqlc_st_bind_double(this.sthandle, i, d);
        }

        public int bindInteger(int i, int i2) {
            if (this.sthandle == 0) {
                return 21;
            }
            return SQLiteNative.sqlc_st_bind_int(this.sthandle, i, i2);
        }

        public int bindLong(int i, long j) {
            if (this.sthandle == 0) {
                return 21;
            }
            return SQLiteNative.sqlc_st_bind_long(this.sthandle, i, j);
        }

        public int bindNull(int i) {
            if (this.sthandle == 0) {
                return 21;
            }
            return SQLiteNative.sqlc_st_bind_null(this.sthandle, i);
        }

        public int bindTextNativeString(int i, String str) {
            if (this.sthandle == 0) {
                return 21;
            }
            return SQLiteNative.sqlc_st_bind_text_native(this.sthandle, i, str);
        }

        public int step() {
            if (this.sthandle == 0) {
                return 21;
            }
            return SQLiteNative.sqlc_st_step(this.sthandle);
        }

        public int getColumnCount() {
            if (this.sthandle == 0) {
                return -1;
            }
            return SQLiteNative.sqlc_st_column_count(this.sthandle);
        }

        public String getColumnName(int i) {
            if (this.sthandle == 0) {
                return null;
            }
            return SQLiteNative.sqlc_st_column_name(this.sthandle, i);
        }

        public int getColumnType(int i) {
            if (this.sthandle == 0) {
                return -1;
            }
            return SQLiteNative.sqlc_st_column_type(this.sthandle, i);
        }

        public double getColumnDouble(int i) {
            if (this.sthandle == 0) {
                return -1.0d;
            }
            return SQLiteNative.sqlc_st_column_double(this.sthandle, i);
        }

        public int getColumnInteger(int i) {
            if (this.sthandle == 0) {
                return -1;
            }
            return SQLiteNative.sqlc_st_column_int(this.sthandle, i);
        }

        public long getColumnLong(int i) {
            if (this.sthandle == 0) {
                return -1;
            }
            return SQLiteNative.sqlc_st_column_long(this.sthandle, i);
        }

        public String getColumnTextNativeString(int i) {
            if (this.sthandle == 0) {
                return null;
            }
            return SQLiteNative.sqlc_st_column_text_native(this.sthandle, i);
        }

        public int finish() {
            if (this.sthandle == 0) {
                return 21;
            }
            long j = this.sthandle;
            this.sql = null;
            this.sthandle = 0;
            return SQLiteNative.sqlc_st_finish(j);
        }
    }

    public SQLGDatabaseHandle(String str, int i) {
        this.dbfilename = null;
        this.openflags = 0;
        this.dbhandle = 0;
        this.dbfilename = str;
        this.openflags = i;
    }

    public int open() {
        if (this.dbfilename == null || this.dbhandle != 0) {
            return 21;
        }
        long sqlc_db_open = SQLiteNative.sqlc_db_open(this.dbfilename, this.openflags);
        if (sqlc_db_open < 0) {
            return (int) (-sqlc_db_open);
        }
        this.dbhandle = sqlc_db_open;
        return 0;
    }

    public int keyNativeString(String str) {
        if (this.dbhandle == 0) {
            return 21;
        }
        return SQLiteNative.sqlc_db_key_native_string(this.dbhandle, str);
    }

    public int close() {
        if (this.dbhandle == 0) {
            return 21;
        }
        return SQLiteNative.sqlc_db_close(this.dbhandle);
    }

    public boolean isOpen() {
        return this.dbhandle != 0;
    }

    public SQLStatementHandle newStatementHandle(String str) {
        if (this.dbhandle == 0) {
            return null;
        }
        return new SQLGStatementHandle(str, null);
    }

    public long getLastInsertRowid() {
        if (this.dbhandle == 0) {
            return -1;
        }
        return SQLiteNative.sqlc_db_last_insert_rowid(this.dbhandle);
    }

    public int getTotalChanges() {
        if (this.dbhandle == 0) {
            return -1;
        }
        return SQLiteNative.sqlc_db_total_changes(this.dbhandle);
    }

    public String getLastErrorMessage() {
        if (this.dbhandle == 0) {
            return null;
        }
        return SQLiteNative.sqlc_db_errmsg_native(this.dbhandle);
    }
}
