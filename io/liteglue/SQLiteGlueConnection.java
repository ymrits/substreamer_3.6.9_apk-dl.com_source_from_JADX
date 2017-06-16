package io.liteglue;

import java.sql.SQLException;

class SQLiteGlueConnection implements SQLiteConnection {
    private SQLDatabaseHandle db;

    private class SQLGStatement implements SQLiteStatement {
        private int columnCount;
        private boolean hasRow;
        private String sql;
        private SQLStatementHandle sthandle;

        SQLGStatement(String str) {
            this.sthandle = null;
            this.sql = null;
            this.hasRow = false;
            this.columnCount = 0;
            this.sql = str;
            this.sthandle = SQLiteGlueConnection.this.db.newStatementHandle(str);
        }

        int prepare() {
            return this.sthandle.prepare();
        }

        public void bindDouble(int i, double d) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            }
            int bindDouble = this.sthandle.bindDouble(i, d);
            if (bindDouble != 0) {
                throw new SQLException("sqlite3_bind_double failure: " + SQLiteGlueConnection.this.db.getLastErrorMessage(), "failure", bindDouble);
            }
        }

        public void bindInteger(int i, int i2) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            }
            int bindInteger = this.sthandle.bindInteger(i, i2);
            if (bindInteger != 0) {
                throw new SQLException("sqlite3_bind_int failure: " + SQLiteGlueConnection.this.db.getLastErrorMessage(), "failure", bindInteger);
            }
        }

        public void bindLong(int i, long j) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            }
            int bindLong = this.sthandle.bindLong(i, j);
            if (bindLong != 0) {
                throw new SQLException("sqlite3_bind_int64 (long) failure: " + SQLiteGlueConnection.this.db.getLastErrorMessage(), "failure", bindLong);
            }
        }

        public void bindNull(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            }
            int bindNull = this.sthandle.bindNull(i);
            if (bindNull != 0) {
                throw new SQLException("sqlite3_bind_null failure: " + SQLiteGlueConnection.this.db.getLastErrorMessage(), "failure", bindNull);
            }
        }

        public void bindTextNativeString(int i, String str) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (str == null) {
                throw new SQLException("null argument", "failed", 21);
            } else {
                int bindTextNativeString = this.sthandle.bindTextNativeString(i, str);
                if (bindTextNativeString != 0) {
                    throw new SQLException("sqlite3_bind_text failure: " + SQLiteGlueConnection.this.db.getLastErrorMessage(), "failure", bindTextNativeString);
                }
            }
        }

        public boolean step() throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            }
            int step = this.sthandle.step();
            if (step == 0 || step == 100 || step == SQLiteNative.SQLC_RESULT_DONE) {
                this.hasRow = step == 100;
                if (this.hasRow) {
                    this.columnCount = this.sthandle.getColumnCount();
                } else {
                    this.columnCount = 0;
                }
                return this.hasRow;
            }
            throw new SQLException("sqlite3_step failure: " + SQLiteGlueConnection.this.db.getLastErrorMessage(), "failure", step);
        }

        public int getColumnCount() throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (this.hasRow) {
                return this.columnCount;
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public String getColumnName(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (!this.hasRow) {
                throw new SQLException("no result available", "failed", 21);
            } else if (i >= 0 && i < this.columnCount) {
                return this.sthandle.getColumnName(i);
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public int getColumnType(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (!this.hasRow) {
                throw new SQLException("no result available", "failed", 21);
            } else if (i >= 0 && i < this.columnCount) {
                return this.sthandle.getColumnType(i);
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public double getColumnDouble(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (!this.hasRow) {
                throw new SQLException("no result available", "failed", 21);
            } else if (i >= 0 && i < this.columnCount) {
                return this.sthandle.getColumnDouble(i);
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public int getColumnInteger(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (!this.hasRow) {
                throw new SQLException("no result available", "failed", 21);
            } else if (i >= 0 && i < this.columnCount) {
                return this.sthandle.getColumnInteger(i);
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public long getColumnLong(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (!this.hasRow) {
                throw new SQLException("no result available", "failed", 21);
            } else if (i >= 0 && i < this.columnCount) {
                return this.sthandle.getColumnLong(i);
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public String getColumnTextNativeString(int i) throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            } else if (!this.hasRow) {
                throw new SQLException("no result available", "failed", 21);
            } else if (i >= 0 && i < this.columnCount) {
                return this.sthandle.getColumnTextNativeString(i);
            } else {
                throw new SQLException("no result available", "failed", 21);
            }
        }

        public void dispose() throws SQLException {
            if (this.sthandle == null) {
                throw new SQLException("already disposed", "failed", 21);
            }
            this.sthandle.finish();
            this.sthandle = null;
        }
    }

    public SQLiteGlueConnection(String str, int i) throws SQLException {
        this.db = null;
        if (str == null) {
            throw new SQLException("null argument", "failed", 21);
        }
        SQLDatabaseHandle sQLGDatabaseHandle = new SQLGDatabaseHandle(str, i);
        int open = sQLGDatabaseHandle.open();
        if (open != 0) {
            throw new SQLException("sqlite3_open_v2 failure: " + this.db.getLastErrorMessage(), "failure", open);
        }
        this.db = sQLGDatabaseHandle;
    }

    public void dispose() throws SQLException {
        if (this.db == null) {
            throw new SQLException("already disposed", "failed", 21);
        }
        int close = this.db.close();
        if (close != 0) {
            throw new SQLException("sqlite3_close failure: " + this.db.getLastErrorMessage(), "failure", close);
        }
        this.db = null;
    }

    public void keyNativeString(String str) throws SQLException {
        if (this.db == null) {
            throw new SQLException("already disposed", "failed", 21);
        }
        int keyNativeString = this.db.keyNativeString(str);
        if (keyNativeString != 0) {
            throw new SQLException("sqlite3_key failure: " + this.db.getLastErrorMessage(), "failure", keyNativeString);
        }
    }

    public SQLiteStatement prepareStatement(String str) throws SQLException {
        if (this.db == null) {
            throw new SQLException("already disposed", "failed", 21);
        } else if (str == null) {
            throw new SQLException("null argument", "failed", 21);
        } else {
            SQLiteStatement sQLGStatement = new SQLGStatement(str);
            int prepare = sQLGStatement.prepare();
            if (prepare == 0) {
                return sQLGStatement;
            }
            throw new SQLException("sqlite3_prepare_v2 failure: " + this.db.getLastErrorMessage(), "failure", prepare);
        }
    }

    public long getLastInsertRowid() throws SQLException {
        if (this.db != null) {
            return this.db.getLastInsertRowid();
        }
        throw new SQLException("already disposed", "failed", 21);
    }

    public int getTotalChanges() throws SQLException {
        if (this.db != null) {
            return this.db.getTotalChanges();
        }
        throw new SQLException("already disposed", "failed", 21);
    }
}
