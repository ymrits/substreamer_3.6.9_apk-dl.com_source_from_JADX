package io.sqlc;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.globalization.Globalization;
import org.apache.cordova.networkinformation.NetworkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SQLiteAndroidDatabase {
    private static final Pattern DELETE_TABLE_NAME;
    private static final Pattern FIRST_WORD;
    private static final Pattern UPDATE_TABLE_NAME;
    private static final Pattern WHERE_CLAUSE;
    private static final boolean isPostHoneycomb;
    File dbFile;
    SQLiteDatabase mydb;

    enum QueryType {
        update,
        insert,
        delete,
        select,
        begin,
        commit,
        rollback,
        other
    }

    SQLiteAndroidDatabase() {
    }

    static {
        FIRST_WORD = Pattern.compile("^\\s*(\\S+)", 2);
        WHERE_CLAUSE = Pattern.compile("\\s+WHERE\\s+(.+)$", 2);
        UPDATE_TABLE_NAME = Pattern.compile("^\\s*UPDATE\\s+(\\S+)", 2);
        DELETE_TABLE_NAME = Pattern.compile("^\\s*DELETE\\s+FROM\\s+(\\S+)", 2);
        isPostHoneycomb = VERSION.SDK_INT >= 11;
    }

    void open(File dbfile) throws Exception {
        this.dbFile = dbfile;
        this.mydb = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
    }

    void closeDatabaseNow() {
        if (this.mydb != null) {
            this.mydb.close();
            this.mydb = null;
        }
    }

    void bugWorkaround() throws Exception {
        closeDatabaseNow();
        open(this.dbFile);
    }

    void executeSqlBatch(String[] queryarr, JSONArray[] jsonparamsArr, CallbackContext cbc) {
        if (this.mydb == null) {
            cbc.error("database has been closed");
            return;
        }
        int len = queryarr.length;
        JSONArray batchResults = new JSONArray();
        for (int i = 0; i < len; i++) {
            executeSqlBatchStatement(queryarr[i], jsonparamsArr[i], batchResults);
        }
        cbc.success(batchResults);
    }

    @SuppressLint({"NewApi"})
    private void executeSqlBatchStatement(String query, JSONArray json_params, JSONArray batchResults) {
        SQLiteConstraintException ex;
        SQLiteException ex2;
        Exception ex3;
        JSONObject queryResult;
        JSONObject r;
        JSONObject er;
        if (this.mydb != null) {
            SQLiteStatement myStatement;
            long insertId;
            int rowsAffectedCompat = 0;
            boolean needRowsAffectedCompat = false;
            JSONObject jSONObject = null;
            String errorMessage = NetworkManager.TYPE_UNKNOWN;
            int code = 0;
            boolean needRawQuery = true;
            QueryType queryType = getQueryType(query);
            if (queryType == QueryType.update || queryType == QueryType.delete) {
                if (isPostHoneycomb) {
                    myStatement = this.mydb.compileStatement(query);
                    if (json_params != null) {
                        bindArgsToStatement(myStatement, json_params);
                    }
                    int rowsAffected = -1;
                    try {
                        rowsAffected = myStatement.executeUpdateDelete();
                        needRawQuery = false;
                    } catch (SQLiteConstraintException ex4) {
                        ex4.printStackTrace();
                        errorMessage = "constraint failure: " + ex4.getMessage();
                        code = 6;
                        Log.v("executeSqlBatch", "SQLiteStatement.executeUpdateDelete(): Error=" + errorMessage);
                        needRawQuery = false;
                    } catch (SQLiteException ex22) {
                        ex22.printStackTrace();
                        errorMessage = ex22.getMessage();
                        Log.v("executeSqlBatch", "SQLiteStatement.executeUpdateDelete(): Error=" + errorMessage);
                        needRawQuery = false;
                    } catch (Exception ex32) {
                        ex32.printStackTrace();
                        Log.v("executeSqlBatch", "SQLiteStatement.executeUpdateDelete(): runtime error (fallback to old API): " + errorMessage);
                    }
                    try {
                        myStatement.close();
                        if (rowsAffected != -1) {
                            queryResult = new JSONObject();
                            queryResult.put("rowsAffected", rowsAffected);
                            jSONObject = queryResult;
                        }
                    } catch (Exception e) {
                        ex32 = e;
                        ex32.printStackTrace();
                        errorMessage = ex32.getMessage();
                        Log.v("executeSqlBatch", "SQLiteAndroidDatabase.executeSql[Batch](): Error=" + errorMessage);
                        if (jSONObject != null) {
                            try {
                                r = new JSONObject();
                                r.put(Globalization.TYPE, "success");
                                r.put("result", jSONObject);
                                batchResults.put(r);
                            } catch (JSONException ex5) {
                                ex5.printStackTrace();
                                Log.v("executeSqlBatch", "SQLiteAndroidDatabase.executeSql[Batch](): Error=" + ex5.getMessage());
                                return;
                            }
                        }
                        r = new JSONObject();
                        r.put(Globalization.TYPE, "error");
                        er = new JSONObject();
                        er.put("message", errorMessage);
                        er.put("code", code);
                        r.put("result", er);
                        batchResults.put(r);
                    }
                }
                if (needRawQuery) {
                    rowsAffectedCompat = countRowsAffectedCompat(queryType, query, json_params, this.mydb);
                    needRowsAffectedCompat = true;
                    queryResult = jSONObject;
                    if (queryType == QueryType.insert && json_params != null) {
                        needRawQuery = false;
                        myStatement = this.mydb.compileStatement(query);
                        bindArgsToStatement(myStatement, json_params);
                        try {
                            insertId = myStatement.executeInsert();
                            jSONObject = new JSONObject();
                            if (insertId == -1) {
                                try {
                                    jSONObject.put("insertId", insertId);
                                    jSONObject.put("rowsAffected", 1);
                                } catch (SQLiteConstraintException e2) {
                                    ex4 = e2;
                                    ex4.printStackTrace();
                                    errorMessage = "constraint failure: " + ex4.getMessage();
                                    code = 6;
                                    Log.v("executeSqlBatch", "SQLiteDatabase.executeInsert(): Error=" + errorMessage);
                                    myStatement.close();
                                    queryResult = jSONObject;
                                    if (queryType == QueryType.begin) {
                                        needRawQuery = false;
                                        try {
                                            this.mydb.beginTransaction();
                                            jSONObject = new JSONObject();
                                            try {
                                                jSONObject.put("rowsAffected", 0);
                                                queryResult = jSONObject;
                                            } catch (SQLiteException e3) {
                                                ex22 = e3;
                                                ex22.printStackTrace();
                                                errorMessage = ex22.getMessage();
                                                Log.v("executeSqlBatch", "SQLiteDatabase.beginTransaction(): Error=" + errorMessage);
                                                queryResult = jSONObject;
                                                if (queryType == QueryType.commit) {
                                                    needRawQuery = false;
                                                    try {
                                                        this.mydb.setTransactionSuccessful();
                                                        this.mydb.endTransaction();
                                                        jSONObject = new JSONObject();
                                                        try {
                                                            jSONObject.put("rowsAffected", 0);
                                                            queryResult = jSONObject;
                                                        } catch (SQLiteException e4) {
                                                            ex22 = e4;
                                                            ex22.printStackTrace();
                                                            errorMessage = ex22.getMessage();
                                                            Log.v("executeSqlBatch", "SQLiteDatabase.setTransactionSuccessful/endTransaction(): Error=" + errorMessage);
                                                            queryResult = jSONObject;
                                                            if (queryType == QueryType.rollback) {
                                                                needRawQuery = false;
                                                                try {
                                                                    this.mydb.endTransaction();
                                                                    jSONObject = new JSONObject();
                                                                    try {
                                                                        jSONObject.put("rowsAffected", 0);
                                                                    } catch (SQLiteException e5) {
                                                                        ex22 = e5;
                                                                        ex22.printStackTrace();
                                                                        errorMessage = ex22.getMessage();
                                                                        Log.v("executeSqlBatch", "SQLiteDatabase.endTransaction(): Error=" + errorMessage);
                                                                        if (needRawQuery) {
                                                                            try {
                                                                                jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                                                            } catch (SQLiteConstraintException ex42) {
                                                                                ex42.printStackTrace();
                                                                                errorMessage = "constraint failure: " + ex42.getMessage();
                                                                                code = 6;
                                                                                Log.v("executeSqlBatch", "Raw query error=" + errorMessage);
                                                                            } catch (SQLiteException ex222) {
                                                                                ex222.printStackTrace();
                                                                                errorMessage = ex222.getMessage();
                                                                                Log.v("executeSqlBatch", "Raw query error=" + errorMessage);
                                                                            }
                                                                            if (needRowsAffectedCompat) {
                                                                                jSONObject.put("rowsAffected", rowsAffectedCompat);
                                                                            }
                                                                        }
                                                                        if (jSONObject != null) {
                                                                            r = new JSONObject();
                                                                            r.put(Globalization.TYPE, "success");
                                                                            r.put("result", jSONObject);
                                                                            batchResults.put(r);
                                                                        }
                                                                        r = new JSONObject();
                                                                        r.put(Globalization.TYPE, "error");
                                                                        er = new JSONObject();
                                                                        er.put("message", errorMessage);
                                                                        er.put("code", code);
                                                                        r.put("result", er);
                                                                        batchResults.put(r);
                                                                    }
                                                                } catch (SQLiteException e6) {
                                                                    ex222 = e6;
                                                                    jSONObject = queryResult;
                                                                    ex222.printStackTrace();
                                                                    errorMessage = ex222.getMessage();
                                                                    Log.v("executeSqlBatch", "SQLiteDatabase.endTransaction(): Error=" + errorMessage);
                                                                    if (needRawQuery) {
                                                                        jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                                                        if (needRowsAffectedCompat) {
                                                                            jSONObject.put("rowsAffected", rowsAffectedCompat);
                                                                        }
                                                                    }
                                                                    if (jSONObject != null) {
                                                                        r = new JSONObject();
                                                                        r.put(Globalization.TYPE, "success");
                                                                        r.put("result", jSONObject);
                                                                        batchResults.put(r);
                                                                    }
                                                                    r = new JSONObject();
                                                                    r.put(Globalization.TYPE, "error");
                                                                    er = new JSONObject();
                                                                    er.put("message", errorMessage);
                                                                    er.put("code", code);
                                                                    r.put("result", er);
                                                                    batchResults.put(r);
                                                                }
                                                            }
                                                            jSONObject = queryResult;
                                                            if (needRawQuery) {
                                                                jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                                                if (needRowsAffectedCompat) {
                                                                    jSONObject.put("rowsAffected", rowsAffectedCompat);
                                                                }
                                                            }
                                                            if (jSONObject != null) {
                                                                r = new JSONObject();
                                                                r.put(Globalization.TYPE, "error");
                                                                er = new JSONObject();
                                                                er.put("message", errorMessage);
                                                                er.put("code", code);
                                                                r.put("result", er);
                                                                batchResults.put(r);
                                                            }
                                                            r = new JSONObject();
                                                            r.put(Globalization.TYPE, "success");
                                                            r.put("result", jSONObject);
                                                            batchResults.put(r);
                                                        }
                                                    } catch (SQLiteException e7) {
                                                        ex222 = e7;
                                                        jSONObject = queryResult;
                                                        ex222.printStackTrace();
                                                        errorMessage = ex222.getMessage();
                                                        Log.v("executeSqlBatch", "SQLiteDatabase.setTransactionSuccessful/endTransaction(): Error=" + errorMessage);
                                                        queryResult = jSONObject;
                                                        if (queryType == QueryType.rollback) {
                                                            jSONObject = queryResult;
                                                        } else {
                                                            needRawQuery = false;
                                                            this.mydb.endTransaction();
                                                            jSONObject = new JSONObject();
                                                            jSONObject.put("rowsAffected", 0);
                                                        }
                                                        if (needRawQuery) {
                                                            jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                                            if (needRowsAffectedCompat) {
                                                                jSONObject.put("rowsAffected", rowsAffectedCompat);
                                                            }
                                                        }
                                                        if (jSONObject != null) {
                                                            r = new JSONObject();
                                                            r.put(Globalization.TYPE, "error");
                                                            er = new JSONObject();
                                                            er.put("message", errorMessage);
                                                            er.put("code", code);
                                                            r.put("result", er);
                                                            batchResults.put(r);
                                                        }
                                                        r = new JSONObject();
                                                        r.put(Globalization.TYPE, "success");
                                                        r.put("result", jSONObject);
                                                        batchResults.put(r);
                                                    }
                                                }
                                                if (queryType == QueryType.rollback) {
                                                    jSONObject = queryResult;
                                                } else {
                                                    needRawQuery = false;
                                                    this.mydb.endTransaction();
                                                    jSONObject = new JSONObject();
                                                    jSONObject.put("rowsAffected", 0);
                                                }
                                                if (needRawQuery) {
                                                    jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                                    if (needRowsAffectedCompat) {
                                                        jSONObject.put("rowsAffected", rowsAffectedCompat);
                                                    }
                                                }
                                                if (jSONObject != null) {
                                                    r = new JSONObject();
                                                    r.put(Globalization.TYPE, "error");
                                                    er = new JSONObject();
                                                    er.put("message", errorMessage);
                                                    er.put("code", code);
                                                    r.put("result", er);
                                                    batchResults.put(r);
                                                }
                                                r = new JSONObject();
                                                r.put(Globalization.TYPE, "success");
                                                r.put("result", jSONObject);
                                                batchResults.put(r);
                                            }
                                        } catch (SQLiteException e8) {
                                            ex222 = e8;
                                            jSONObject = queryResult;
                                            ex222.printStackTrace();
                                            errorMessage = ex222.getMessage();
                                            Log.v("executeSqlBatch", "SQLiteDatabase.beginTransaction(): Error=" + errorMessage);
                                            queryResult = jSONObject;
                                            if (queryType == QueryType.commit) {
                                                needRawQuery = false;
                                                this.mydb.setTransactionSuccessful();
                                                this.mydb.endTransaction();
                                                jSONObject = new JSONObject();
                                                jSONObject.put("rowsAffected", 0);
                                                queryResult = jSONObject;
                                            }
                                            if (queryType == QueryType.rollback) {
                                                needRawQuery = false;
                                                this.mydb.endTransaction();
                                                jSONObject = new JSONObject();
                                                jSONObject.put("rowsAffected", 0);
                                            } else {
                                                jSONObject = queryResult;
                                            }
                                            if (needRawQuery) {
                                                jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                                if (needRowsAffectedCompat) {
                                                    jSONObject.put("rowsAffected", rowsAffectedCompat);
                                                }
                                            }
                                            if (jSONObject != null) {
                                                r = new JSONObject();
                                                r.put(Globalization.TYPE, "success");
                                                r.put("result", jSONObject);
                                                batchResults.put(r);
                                            }
                                            r = new JSONObject();
                                            r.put(Globalization.TYPE, "error");
                                            er = new JSONObject();
                                            er.put("message", errorMessage);
                                            er.put("code", code);
                                            r.put("result", er);
                                            batchResults.put(r);
                                        }
                                    }
                                    if (queryType == QueryType.commit) {
                                        needRawQuery = false;
                                        this.mydb.setTransactionSuccessful();
                                        this.mydb.endTransaction();
                                        jSONObject = new JSONObject();
                                        jSONObject.put("rowsAffected", 0);
                                        queryResult = jSONObject;
                                    }
                                    if (queryType == QueryType.rollback) {
                                        jSONObject = queryResult;
                                    } else {
                                        needRawQuery = false;
                                        this.mydb.endTransaction();
                                        jSONObject = new JSONObject();
                                        jSONObject.put("rowsAffected", 0);
                                    }
                                    if (needRawQuery) {
                                        jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                        if (needRowsAffectedCompat) {
                                            jSONObject.put("rowsAffected", rowsAffectedCompat);
                                        }
                                    }
                                    if (jSONObject != null) {
                                        r = new JSONObject();
                                        r.put(Globalization.TYPE, "success");
                                        r.put("result", jSONObject);
                                        batchResults.put(r);
                                    }
                                    r = new JSONObject();
                                    r.put(Globalization.TYPE, "error");
                                    er = new JSONObject();
                                    er.put("message", errorMessage);
                                    er.put("code", code);
                                    r.put("result", er);
                                    batchResults.put(r);
                                } catch (SQLiteException e9) {
                                    ex222 = e9;
                                    ex222.printStackTrace();
                                    errorMessage = ex222.getMessage();
                                    Log.v("executeSqlBatch", "SQLiteDatabase.executeInsert(): Error=" + errorMessage);
                                    myStatement.close();
                                    queryResult = jSONObject;
                                    if (queryType == QueryType.begin) {
                                        needRawQuery = false;
                                        this.mydb.beginTransaction();
                                        jSONObject = new JSONObject();
                                        jSONObject.put("rowsAffected", 0);
                                        queryResult = jSONObject;
                                    }
                                    if (queryType == QueryType.commit) {
                                        needRawQuery = false;
                                        this.mydb.setTransactionSuccessful();
                                        this.mydb.endTransaction();
                                        jSONObject = new JSONObject();
                                        jSONObject.put("rowsAffected", 0);
                                        queryResult = jSONObject;
                                    }
                                    if (queryType == QueryType.rollback) {
                                        needRawQuery = false;
                                        this.mydb.endTransaction();
                                        jSONObject = new JSONObject();
                                        jSONObject.put("rowsAffected", 0);
                                    } else {
                                        jSONObject = queryResult;
                                    }
                                    if (needRawQuery) {
                                        jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                        if (needRowsAffectedCompat) {
                                            jSONObject.put("rowsAffected", rowsAffectedCompat);
                                        }
                                    }
                                    if (jSONObject != null) {
                                        r = new JSONObject();
                                        r.put(Globalization.TYPE, "error");
                                        er = new JSONObject();
                                        er.put("message", errorMessage);
                                        er.put("code", code);
                                        r.put("result", er);
                                        batchResults.put(r);
                                    }
                                    r = new JSONObject();
                                    r.put(Globalization.TYPE, "success");
                                    r.put("result", jSONObject);
                                    batchResults.put(r);
                                }
                                myStatement.close();
                                queryResult = jSONObject;
                            } else {
                                jSONObject.put("rowsAffected", 0);
                                myStatement.close();
                                queryResult = jSONObject;
                            }
                        } catch (SQLiteConstraintException e10) {
                            ex42 = e10;
                            jSONObject = queryResult;
                            ex42.printStackTrace();
                            errorMessage = "constraint failure: " + ex42.getMessage();
                            code = 6;
                            Log.v("executeSqlBatch", "SQLiteDatabase.executeInsert(): Error=" + errorMessage);
                            myStatement.close();
                            queryResult = jSONObject;
                            if (queryType == QueryType.begin) {
                                needRawQuery = false;
                                this.mydb.beginTransaction();
                                jSONObject = new JSONObject();
                                jSONObject.put("rowsAffected", 0);
                                queryResult = jSONObject;
                            }
                            if (queryType == QueryType.commit) {
                                needRawQuery = false;
                                this.mydb.setTransactionSuccessful();
                                this.mydb.endTransaction();
                                jSONObject = new JSONObject();
                                jSONObject.put("rowsAffected", 0);
                                queryResult = jSONObject;
                            }
                            if (queryType == QueryType.rollback) {
                                jSONObject = queryResult;
                            } else {
                                needRawQuery = false;
                                this.mydb.endTransaction();
                                jSONObject = new JSONObject();
                                jSONObject.put("rowsAffected", 0);
                            }
                            if (needRawQuery) {
                                jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                if (needRowsAffectedCompat) {
                                    jSONObject.put("rowsAffected", rowsAffectedCompat);
                                }
                            }
                            if (jSONObject != null) {
                                r = new JSONObject();
                                r.put(Globalization.TYPE, "error");
                                er = new JSONObject();
                                er.put("message", errorMessage);
                                er.put("code", code);
                                r.put("result", er);
                                batchResults.put(r);
                            }
                            r = new JSONObject();
                            r.put(Globalization.TYPE, "success");
                            r.put("result", jSONObject);
                            batchResults.put(r);
                        } catch (SQLiteException e11) {
                            ex222 = e11;
                            jSONObject = queryResult;
                            ex222.printStackTrace();
                            errorMessage = ex222.getMessage();
                            Log.v("executeSqlBatch", "SQLiteDatabase.executeInsert(): Error=" + errorMessage);
                            myStatement.close();
                            queryResult = jSONObject;
                            if (queryType == QueryType.begin) {
                                needRawQuery = false;
                                this.mydb.beginTransaction();
                                jSONObject = new JSONObject();
                                jSONObject.put("rowsAffected", 0);
                                queryResult = jSONObject;
                            }
                            if (queryType == QueryType.commit) {
                                needRawQuery = false;
                                this.mydb.setTransactionSuccessful();
                                this.mydb.endTransaction();
                                jSONObject = new JSONObject();
                                jSONObject.put("rowsAffected", 0);
                                queryResult = jSONObject;
                            }
                            if (queryType == QueryType.rollback) {
                                needRawQuery = false;
                                this.mydb.endTransaction();
                                jSONObject = new JSONObject();
                                jSONObject.put("rowsAffected", 0);
                            } else {
                                jSONObject = queryResult;
                            }
                            if (needRawQuery) {
                                jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                                if (needRowsAffectedCompat) {
                                    jSONObject.put("rowsAffected", rowsAffectedCompat);
                                }
                            }
                            if (jSONObject != null) {
                                r = new JSONObject();
                                r.put(Globalization.TYPE, "success");
                                r.put("result", jSONObject);
                                batchResults.put(r);
                            }
                            r = new JSONObject();
                            r.put(Globalization.TYPE, "error");
                            er = new JSONObject();
                            er.put("message", errorMessage);
                            er.put("code", code);
                            r.put("result", er);
                            batchResults.put(r);
                        }
                    }
                    if (queryType == QueryType.begin) {
                        needRawQuery = false;
                        this.mydb.beginTransaction();
                        jSONObject = new JSONObject();
                        jSONObject.put("rowsAffected", 0);
                        queryResult = jSONObject;
                    }
                    if (queryType == QueryType.commit) {
                        needRawQuery = false;
                        this.mydb.setTransactionSuccessful();
                        this.mydb.endTransaction();
                        jSONObject = new JSONObject();
                        jSONObject.put("rowsAffected", 0);
                        queryResult = jSONObject;
                    }
                    if (queryType == QueryType.rollback) {
                        needRawQuery = false;
                        this.mydb.endTransaction();
                        jSONObject = new JSONObject();
                        jSONObject.put("rowsAffected", 0);
                    } else {
                        jSONObject = queryResult;
                    }
                    if (needRawQuery) {
                        jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                        if (needRowsAffectedCompat) {
                            jSONObject.put("rowsAffected", rowsAffectedCompat);
                        }
                    }
                    if (jSONObject != null) {
                        r = new JSONObject();
                        r.put(Globalization.TYPE, "success");
                        r.put("result", jSONObject);
                        batchResults.put(r);
                    }
                    r = new JSONObject();
                    r.put(Globalization.TYPE, "error");
                    er = new JSONObject();
                    er.put("message", errorMessage);
                    er.put("code", code);
                    r.put("result", er);
                    batchResults.put(r);
                }
            }
            queryResult = jSONObject;
            try {
                needRawQuery = false;
                myStatement = this.mydb.compileStatement(query);
                bindArgsToStatement(myStatement, json_params);
                insertId = myStatement.executeInsert();
                jSONObject = new JSONObject();
                if (insertId == -1) {
                    jSONObject.put("rowsAffected", 0);
                    myStatement.close();
                    queryResult = jSONObject;
                    if (queryType == QueryType.begin) {
                        needRawQuery = false;
                        this.mydb.beginTransaction();
                        jSONObject = new JSONObject();
                        jSONObject.put("rowsAffected", 0);
                        queryResult = jSONObject;
                    }
                    if (queryType == QueryType.commit) {
                        needRawQuery = false;
                        this.mydb.setTransactionSuccessful();
                        this.mydb.endTransaction();
                        jSONObject = new JSONObject();
                        jSONObject.put("rowsAffected", 0);
                        queryResult = jSONObject;
                    }
                    if (queryType == QueryType.rollback) {
                        jSONObject = queryResult;
                    } else {
                        needRawQuery = false;
                        this.mydb.endTransaction();
                        jSONObject = new JSONObject();
                        jSONObject.put("rowsAffected", 0);
                    }
                    if (needRawQuery) {
                        jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                        if (needRowsAffectedCompat) {
                            jSONObject.put("rowsAffected", rowsAffectedCompat);
                        }
                    }
                    if (jSONObject != null) {
                        r = new JSONObject();
                        r.put(Globalization.TYPE, "error");
                        er = new JSONObject();
                        er.put("message", errorMessage);
                        er.put("code", code);
                        r.put("result", er);
                        batchResults.put(r);
                    }
                    r = new JSONObject();
                    r.put(Globalization.TYPE, "success");
                    r.put("result", jSONObject);
                    batchResults.put(r);
                }
                jSONObject.put("insertId", insertId);
                jSONObject.put("rowsAffected", 1);
                myStatement.close();
                queryResult = jSONObject;
                if (queryType == QueryType.begin) {
                    needRawQuery = false;
                    this.mydb.beginTransaction();
                    jSONObject = new JSONObject();
                    jSONObject.put("rowsAffected", 0);
                    queryResult = jSONObject;
                }
                if (queryType == QueryType.commit) {
                    needRawQuery = false;
                    this.mydb.setTransactionSuccessful();
                    this.mydb.endTransaction();
                    jSONObject = new JSONObject();
                    jSONObject.put("rowsAffected", 0);
                    queryResult = jSONObject;
                }
                if (queryType == QueryType.rollback) {
                    needRawQuery = false;
                    this.mydb.endTransaction();
                    jSONObject = new JSONObject();
                    jSONObject.put("rowsAffected", 0);
                } else {
                    jSONObject = queryResult;
                }
                if (needRawQuery) {
                    jSONObject = executeSqlStatementQuery(this.mydb, query, json_params);
                    if (needRowsAffectedCompat) {
                        jSONObject.put("rowsAffected", rowsAffectedCompat);
                    }
                }
                if (jSONObject != null) {
                    r = new JSONObject();
                    r.put(Globalization.TYPE, "success");
                    r.put("result", jSONObject);
                    batchResults.put(r);
                }
                r = new JSONObject();
                r.put(Globalization.TYPE, "error");
                er = new JSONObject();
                er.put("message", errorMessage);
                er.put("code", code);
                r.put("result", er);
                batchResults.put(r);
            } catch (Exception e12) {
                ex32 = e12;
                jSONObject = queryResult;
                ex32.printStackTrace();
                errorMessage = ex32.getMessage();
                Log.v("executeSqlBatch", "SQLiteAndroidDatabase.executeSql[Batch](): Error=" + errorMessage);
                if (jSONObject != null) {
                    r = new JSONObject();
                    r.put(Globalization.TYPE, "error");
                    er = new JSONObject();
                    er.put("message", errorMessage);
                    er.put("code", code);
                    r.put("result", er);
                    batchResults.put(r);
                }
                r = new JSONObject();
                r.put(Globalization.TYPE, "success");
                r.put("result", jSONObject);
                batchResults.put(r);
            }
        }
    }

    private final int countRowsAffectedCompat(QueryType queryType, String query, JSONArray json_params, SQLiteDatabase mydb) throws JSONException {
        int j;
        Matcher whereMatcher = WHERE_CLAUSE.matcher(query);
        String where = BuildConfig.FLAVOR;
        for (int pos = 0; whereMatcher.find(pos); pos = whereMatcher.start(1)) {
            where = " WHERE " + whereMatcher.group(1);
        }
        int numQuestionMarks = 0;
        for (j = 0; j < where.length(); j++) {
            if (where.charAt(j) == '?') {
                numQuestionMarks++;
            }
        }
        JSONArray subParams = null;
        if (json_params != null) {
            JSONArray origArray = json_params;
            subParams = new JSONArray();
            int startPos = origArray.length() - numQuestionMarks;
            for (j = startPos; j < origArray.length(); j++) {
                subParams.put(j - startPos, origArray.get(j));
            }
        }
        Matcher tableMatcher;
        SQLiteStatement statement;
        if (queryType == QueryType.update) {
            tableMatcher = UPDATE_TABLE_NAME.matcher(query);
            if (tableMatcher.find()) {
                try {
                    statement = mydb.compileStatement("SELECT count(*) FROM " + tableMatcher.group(1) + where);
                    if (subParams != null) {
                        bindArgsToStatement(statement, subParams);
                    }
                    return (int) statement.simpleQueryForLong();
                } catch (Exception e) {
                    Log.e(SQLiteAndroidDatabase.class.getSimpleName(), "uncaught", e);
                }
            }
        } else {
            tableMatcher = DELETE_TABLE_NAME.matcher(query);
            if (tableMatcher.find()) {
                try {
                    statement = mydb.compileStatement("SELECT count(*) FROM " + tableMatcher.group(1) + where);
                    bindArgsToStatement(statement, subParams);
                    return (int) statement.simpleQueryForLong();
                } catch (Exception e2) {
                    Log.e(SQLiteAndroidDatabase.class.getSimpleName(), "uncaught", e2);
                }
            }
        }
        return 0;
    }

    private void bindArgsToStatement(SQLiteStatement myStatement, JSONArray sqlArgs) throws JSONException {
        int i = 0;
        while (i < sqlArgs.length()) {
            if ((sqlArgs.get(i) instanceof Float) || (sqlArgs.get(i) instanceof Double)) {
                myStatement.bindDouble(i + 1, sqlArgs.getDouble(i));
            } else if (sqlArgs.get(i) instanceof Number) {
                myStatement.bindLong(i + 1, sqlArgs.getLong(i));
            } else if (sqlArgs.isNull(i)) {
                myStatement.bindNull(i + 1);
            } else {
                myStatement.bindString(i + 1, sqlArgs.getString(i));
            }
            i++;
        }
    }

    private JSONObject executeSqlStatementQuery(SQLiteDatabase mydb, String query, JSONArray paramsAsJson) throws Exception {
        JSONObject rowsResult = new JSONObject();
        try {
            String[] params = new String[paramsAsJson.length()];
            for (int j = 0; j < paramsAsJson.length(); j++) {
                if (paramsAsJson.isNull(j)) {
                    params[j] = BuildConfig.FLAVOR;
                } else {
                    params[j] = paramsAsJson.getString(j);
                }
            }
            Cursor cur = mydb.rawQuery(query, params);
            if (cur != null && cur.moveToFirst()) {
                JSONArray rowsArrayResult = new JSONArray();
                String key = BuildConfig.FLAVOR;
                int colCount = cur.getColumnCount();
                do {
                    JSONObject row = new JSONObject();
                    int i = 0;
                    while (i < colCount) {
                        try {
                            key = cur.getColumnName(i);
                            if (isPostHoneycomb) {
                                try {
                                    bindPostHoneycomb(row, key, cur, i);
                                } catch (Exception e) {
                                    bindPreHoneycomb(row, key, cur, i);
                                }
                            } else {
                                bindPreHoneycomb(row, key, cur, i);
                            }
                            i++;
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                    }
                    rowsArrayResult.put(row);
                } while (cur.moveToNext());
                try {
                    rowsResult.put("rows", rowsArrayResult);
                } catch (JSONException e22) {
                    e22.printStackTrace();
                }
            }
            if (cur != null) {
                cur.close();
            }
            return rowsResult;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.v("executeSqlBatch", "SQLiteAndroidDatabase.executeSql[Batch](): Error=" + ex.getMessage());
            throw ex;
        }
    }

    @SuppressLint({"NewApi"})
    private void bindPostHoneycomb(JSONObject row, String key, Cursor cur, int i) throws JSONException {
        switch (cur.getType(i)) {
            case FileUtils.ACTION_GET_FILE /*0*/:
                row.put(key, JSONObject.NULL);
            case FileUtils.ACTION_WRITE /*1*/:
                row.put(key, cur.getLong(i));
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                row.put(key, cur.getDouble(i));
            default:
                row.put(key, cur.getString(i));
        }
    }

    private void bindPreHoneycomb(JSONObject row, String key, Cursor cursor, int i) throws JSONException {
        CursorWindow cursorWindow = ((SQLiteCursor) cursor).getWindow();
        int pos = cursor.getPosition();
        if (cursorWindow.isNull(pos, i)) {
            row.put(key, JSONObject.NULL);
        } else if (cursorWindow.isLong(pos, i)) {
            row.put(key, cursor.getLong(i));
        } else if (cursorWindow.isFloat(pos, i)) {
            row.put(key, cursor.getDouble(i));
        } else {
            row.put(key, cursor.getString(i));
        }
    }

    static QueryType getQueryType(String query) {
        Matcher matcher = FIRST_WORD.matcher(query);
        if (matcher.find()) {
            try {
                return QueryType.valueOf(matcher.group(1).toLowerCase());
            } catch (IllegalArgumentException e) {
            }
        }
        return QueryType.other;
    }
}
