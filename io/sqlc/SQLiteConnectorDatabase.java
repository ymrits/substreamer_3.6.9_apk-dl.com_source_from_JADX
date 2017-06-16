package io.sqlc;

import android.support.v4.app.NotificationCompat.WearableExtender;
import android.util.Log;
import com.ghenry22.substream2.C0173R;
import io.liteglue.SQLiteConnection;
import io.liteglue.SQLiteConnector;
import io.liteglue.SQLiteNative;
import io.liteglue.SQLiteStatement;
import java.io.File;
import java.sql.SQLException;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.globalization.Globalization;
import org.apache.cordova.networkinformation.NetworkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SQLiteConnectorDatabase extends SQLiteAndroidDatabase {
    static SQLiteConnector connector;
    SQLiteConnection mydb;

    SQLiteConnectorDatabase() {
    }

    static {
        connector = new SQLiteConnector();
    }

    void open(File dbFile) throws Exception {
        this.mydb = connector.newSQLiteConnection(dbFile.getAbsolutePath(), 6);
    }

    void closeDatabaseNow() {
        try {
            if (this.mydb != null) {
                this.mydb.dispose();
            }
        } catch (Exception e) {
            Log.e(SQLitePlugin.class.getSimpleName(), "couldn't close database, ignoring", e);
        }
    }

    void bugWorkaround() {
    }

    void executeSqlBatch(String[] queryarr, JSONArray[] jsonparams, CallbackContext cbc) {
        if (this.mydb == null) {
            cbc.error("database has been closed");
            return;
        }
        int len = queryarr.length;
        JSONArray batchResults = new JSONArray();
        for (int i = 0; i < len; i++) {
            JSONObject queryResult = null;
            String errorMessage = NetworkManager.TYPE_UNKNOWN;
            int code = 0;
            try {
                String query = queryarr[i];
                long lastTotal = (long) this.mydb.getTotalChanges();
                queryResult = executeSQLiteStatement(query, jsonparams[i], cbc);
                long rowsAffected = ((long) this.mydb.getTotalChanges()) - lastTotal;
                queryResult.put("rowsAffected", rowsAffected);
                if (rowsAffected > 0) {
                    long insertId = this.mydb.getLastInsertRowid();
                    if (insertId > 0) {
                        queryResult.put("insertId", insertId);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                int sqliteErrorCode = ex.getErrorCode();
                errorMessage = ex.getMessage();
                Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): SQL Error code = " + sqliteErrorCode + " message = " + errorMessage);
                switch (sqliteErrorCode) {
                    case FileUtils.ACTION_WRITE /*1*/:
                        code = 5;
                        break;
                    case C0173R.styleable.Toolbar_subtitleTextAppearance /*13*/:
                        code = 4;
                        break;
                    case SQLiteNative.SQLC_RESULT_CONSTRAINT /*19*/:
                        code = 6;
                        break;
                    default:
                        break;
                }
            } catch (JSONException ex2) {
                ex2.printStackTrace();
                errorMessage = ex2.getMessage();
                code = 0;
                Log.e("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): UNEXPECTED JSON Error=" + errorMessage);
            }
            JSONObject r;
            if (queryResult != null) {
                try {
                    r = new JSONObject();
                    r.put(Globalization.TYPE, "success");
                    r.put("result", queryResult);
                    batchResults.put(r);
                } catch (JSONException ex22) {
                    ex22.printStackTrace();
                    Log.e("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + ex22.getMessage());
                }
            } else {
                r = new JSONObject();
                r.put(Globalization.TYPE, "error");
                JSONObject er = new JSONObject();
                er.put("message", errorMessage);
                er.put("code", code);
                r.put("result", er);
                batchResults.put(r);
            }
        }
        cbc.success(batchResults);
    }

    private JSONObject executeSQLiteStatement(String query, JSONArray paramsAsJson, CallbackContext cbc) throws JSONException, SQLException {
        String errorMessage;
        JSONObject rowsResult = new JSONObject();
        SQLiteStatement myStatement = this.mydb.prepareStatement(query);
        try {
            int i;
            String[] params = new String[paramsAsJson.length()];
            for (i = 0; i < paramsAsJson.length(); i++) {
                if (paramsAsJson.isNull(i)) {
                    myStatement.bindNull(i + 1);
                } else {
                    Object p = paramsAsJson.get(i);
                    if ((p instanceof Float) || (p instanceof Double)) {
                        myStatement.bindDouble(i + 1, paramsAsJson.getDouble(i));
                    } else if (p instanceof Number) {
                        myStatement.bindLong(i + 1, paramsAsJson.getLong(i));
                    } else {
                        int i2 = i + 1;
                        myStatement.bindTextNativeString(i2, paramsAsJson.getString(i));
                    }
                }
            }
            if (myStatement.step()) {
                JSONArray rowsArrayResult = new JSONArray();
                String key = BuildConfig.FLAVOR;
                int colCount = myStatement.getColumnCount();
                do {
                    JSONObject row = new JSONObject();
                    i = 0;
                    while (i < colCount) {
                        try {
                            key = myStatement.getColumnName(i);
                            switch (myStatement.getColumnType(i)) {
                                case FileUtils.ACTION_WRITE /*1*/:
                                    row.put(key, myStatement.getColumnLong(i));
                                    break;
                                case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                                    row.put(key, myStatement.getColumnDouble(i));
                                    break;
                                case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                                    row.put(key, JSONObject.NULL);
                                    break;
                                default:
                                    row.put(key, myStatement.getColumnTextNativeString(i));
                                    break;
                            }
                            i++;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    rowsArrayResult.put(row);
                } while (myStatement.step());
                try {
                    rowsResult.put("rows", rowsArrayResult);
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
            myStatement.dispose();
            return rowsResult;
        } catch (SQLException ex) {
            ex.printStackTrace();
            errorMessage = ex.getMessage();
            Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + errorMessage);
            myStatement.dispose();
            throw ex;
        } catch (JSONException ex2) {
            ex2.printStackTrace();
            errorMessage = ex2.getMessage();
            Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + errorMessage);
            myStatement.dispose();
            throw ex2;
        }
    }
}
