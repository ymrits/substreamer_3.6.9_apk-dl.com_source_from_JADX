package io.sqlc;

import android.support.v4.app.NotificationCompat.WearableExtender;
import android.util.Log;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SQLitePlugin extends CordovaPlugin {
    static ConcurrentHashMap<String, DBRunner> dbrmap;

    /* renamed from: io.sqlc.SQLitePlugin.1 */
    static /* synthetic */ class C01851 {
        static final /* synthetic */ int[] $SwitchMap$io$sqlc$SQLitePlugin$Action;

        static {
            $SwitchMap$io$sqlc$SQLitePlugin$Action = new int[Action.values().length];
            try {
                $SwitchMap$io$sqlc$SQLitePlugin$Action[Action.echoStringValue.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$io$sqlc$SQLitePlugin$Action[Action.open.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$io$sqlc$SQLitePlugin$Action[Action.close.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$io$sqlc$SQLitePlugin$Action[Action.delete.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$io$sqlc$SQLitePlugin$Action[Action.executeSqlBatch.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$io$sqlc$SQLitePlugin$Action[Action.backgroundExecuteSqlBatch.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private enum Action {
        echoStringValue,
        open,
        close,
        delete,
        executeSqlBatch,
        backgroundExecuteSqlBatch
    }

    private final class DBQuery {
        final CallbackContext cbc;
        final boolean close;
        final boolean delete;
        final JSONArray[] jsonparams;
        final String[] queries;
        final boolean stop;

        DBQuery(String[] myqueries, JSONArray[] params, CallbackContext c) {
            this.stop = false;
            this.close = false;
            this.delete = false;
            this.queries = myqueries;
            this.jsonparams = params;
            this.cbc = c;
        }

        DBQuery(boolean delete, CallbackContext cbc) {
            this.stop = true;
            this.close = true;
            this.delete = delete;
            this.queries = null;
            this.jsonparams = null;
            this.cbc = cbc;
        }

        DBQuery() {
            this.stop = true;
            this.close = false;
            this.delete = false;
            this.queries = null;
            this.jsonparams = null;
            this.cbc = null;
        }
    }

    private class DBRunner implements Runnable {
        private boolean bugWorkaround;
        final String dbname;
        SQLiteAndroidDatabase mydb;
        private boolean oldImpl;
        final CallbackContext openCbc;
        final BlockingQueue<DBQuery> f3q;

        DBRunner(String dbname, JSONObject options, CallbackContext cbc) {
            this.dbname = dbname;
            this.oldImpl = options.has("androidOldDatabaseImplementation");
            Log.v(SQLitePlugin.class.getSimpleName(), "Android db implementation: built-in android.database.sqlite package");
            boolean z = this.oldImpl && options.has("androidBugWorkaround");
            this.bugWorkaround = z;
            if (this.bugWorkaround) {
                Log.v(SQLitePlugin.class.getSimpleName(), "Android db closing/locking workaround applied");
            }
            this.f3q = new LinkedBlockingQueue();
            this.openCbc = cbc;
        }

        public void run() {
            try {
                this.mydb = SQLitePlugin.this.openDatabase(this.dbname, this.openCbc, this.oldImpl);
                DBQuery dbq = null;
                try {
                    dbq = (DBQuery) this.f3q.take();
                    while (!dbq.stop) {
                        this.mydb.executeSqlBatch(dbq.queries, dbq.jsonparams, dbq.cbc);
                        if (this.bugWorkaround && dbq.queries.length == 1 && dbq.queries[0] == "COMMIT") {
                            this.mydb.bugWorkaround();
                        }
                        dbq = (DBQuery) this.f3q.take();
                    }
                } catch (Exception e) {
                    Log.e(SQLitePlugin.class.getSimpleName(), "unexpected error", e);
                }
                if (dbq != null && dbq.close) {
                    try {
                        SQLitePlugin.this.closeDatabaseNow(this.dbname);
                        SQLitePlugin.dbrmap.remove(this.dbname);
                        if (dbq.delete) {
                            try {
                                if (SQLitePlugin.this.deleteDatabaseNow(this.dbname)) {
                                    dbq.cbc.success();
                                    return;
                                } else {
                                    dbq.cbc.error("couldn't delete database");
                                    return;
                                }
                            } catch (Exception e2) {
                                Log.e(SQLitePlugin.class.getSimpleName(), "couldn't delete database", e2);
                                dbq.cbc.error("couldn't delete database: " + e2);
                                return;
                            }
                        }
                        dbq.cbc.success();
                    } catch (Exception e22) {
                        Log.e(SQLitePlugin.class.getSimpleName(), "couldn't close database", e22);
                        if (dbq.cbc != null) {
                            dbq.cbc.error("couldn't close database: " + e22);
                        }
                    }
                }
            } catch (Exception e222) {
                Log.e(SQLitePlugin.class.getSimpleName(), "unexpected error, stopping db thread", e222);
                SQLitePlugin.dbrmap.remove(this.dbname);
            }
        }
    }

    static {
        dbrmap = new ConcurrentHashMap();
    }

    public boolean execute(String actionAsString, JSONArray args, CallbackContext cbc) {
        boolean z = false;
        try {
            try {
                z = executeAndPossiblyThrow(Action.valueOf(actionAsString), args, cbc);
            } catch (JSONException e) {
                Log.e(SQLitePlugin.class.getSimpleName(), "unexpected error", e);
            }
        } catch (IllegalArgumentException e2) {
            Log.e(SQLitePlugin.class.getSimpleName(), "unexpected error", e2);
        }
        return z;
    }

    private boolean executeAndPossiblyThrow(Action action, JSONArray args, CallbackContext cbc) throws JSONException {
        switch (C01851.$SwitchMap$io$sqlc$SQLitePlugin$Action[action.ordinal()]) {
            case FileUtils.ACTION_WRITE /*1*/:
                cbc.success(args.getJSONObject(0).getString("value"));
                break;
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                JSONObject o = args.getJSONObject(0);
                startDatabase(o.getString("name"), o, cbc);
                break;
            case FileUtils.WRITE /*3*/:
                closeDatabase(args.getJSONObject(0).getString("path"), cbc);
                break;
            case FileUtils.READ /*4*/:
                deleteDatabase(args.getJSONObject(0).getString("path"), cbc);
                break;
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                JSONObject allargs = args.getJSONObject(0);
                String str = "dbname";
                String dbname = allargs.getJSONObject("dbargs").getString(r17);
                JSONArray txargs = allargs.getJSONArray("executes");
                if (!txargs.isNull(0)) {
                    int len = txargs.length();
                    String[] queries = new String[len];
                    JSONArray[] jsonparams = new JSONArray[len];
                    for (int i = 0; i < len; i++) {
                        JSONObject a = txargs.getJSONObject(i);
                        queries[i] = a.getString("sql");
                        jsonparams[i] = a.getJSONArray("params");
                    }
                    DBQuery q = new DBQuery(queries, jsonparams, cbc);
                    DBRunner r = (DBRunner) dbrmap.get(dbname);
                    if (r == null) {
                        cbc.error("database not open");
                        break;
                    }
                    try {
                        r.f3q.put(q);
                        break;
                    } catch (Exception e) {
                        Log.e(SQLitePlugin.class.getSimpleName(), "couldn't add to queue", e);
                        cbc.error("couldn't add to queue");
                        break;
                    }
                }
                cbc.error("missing executes list");
                break;
        }
        return true;
    }

    public void onDestroy() {
        while (!dbrmap.isEmpty()) {
            String dbname = (String) dbrmap.keySet().iterator().next();
            closeDatabaseNow(dbname);
            try {
                ((DBRunner) dbrmap.get(dbname)).f3q.put(new DBQuery());
            } catch (Exception e) {
                Log.e(SQLitePlugin.class.getSimpleName(), "couldn't stop db thread", e);
            }
            dbrmap.remove(dbname);
        }
    }

    private void startDatabase(String dbname, JSONObject options, CallbackContext cbc) {
        if (((DBRunner) dbrmap.get(dbname)) != null) {
            cbc.success();
            return;
        }
        DBRunner r = new DBRunner(dbname, options, cbc);
        dbrmap.put(dbname, r);
        this.cordova.getThreadPool().execute(r);
    }

    private SQLiteAndroidDatabase openDatabase(String dbname, CallbackContext cbc, boolean old_impl) throws Exception {
        try {
            File dbfile = this.cordova.getActivity().getDatabasePath(dbname);
            if (!dbfile.exists()) {
                dbfile.getParentFile().mkdirs();
            }
            Log.v("info", "Open sqlite db: " + dbfile.getAbsolutePath());
            SQLiteAndroidDatabase mydb = old_impl ? new SQLiteAndroidDatabase() : new SQLiteConnectorDatabase();
            mydb.open(dbfile);
            if (cbc != null) {
                cbc.success();
            }
            return mydb;
        } catch (Exception e) {
            if (cbc != null) {
                cbc.error("can't open database " + e);
            }
            throw e;
        }
    }

    private void closeDatabase(String dbname, CallbackContext cbc) {
        DBRunner r = (DBRunner) dbrmap.get(dbname);
        if (r != null) {
            try {
                r.f3q.put(new DBQuery(false, cbc));
            } catch (Exception e) {
                if (cbc != null) {
                    cbc.error("couldn't close database" + e);
                }
                Log.e(SQLitePlugin.class.getSimpleName(), "couldn't close database", e);
            }
        } else if (cbc != null) {
            cbc.success();
        }
    }

    private void closeDatabaseNow(String dbname) {
        DBRunner r = (DBRunner) dbrmap.get(dbname);
        if (r != null) {
            SQLiteAndroidDatabase mydb = r.mydb;
            if (mydb != null) {
                mydb.closeDatabaseNow();
            }
        }
    }

    private void deleteDatabase(String dbname, CallbackContext cbc) {
        DBRunner r = (DBRunner) dbrmap.get(dbname);
        if (r != null) {
            try {
                r.f3q.put(new DBQuery(true, cbc));
            } catch (Exception e) {
                if (cbc != null) {
                    cbc.error("couldn't close database" + e);
                }
                Log.e(SQLitePlugin.class.getSimpleName(), "couldn't close database", e);
            }
        } else if (deleteDatabaseNow(dbname)) {
            cbc.success();
        } else {
            cbc.error("couldn't delete database");
        }
    }

    private boolean deleteDatabaseNow(String dbname) {
        try {
            return this.cordova.getActivity().deleteDatabase(this.cordova.getActivity().getDatabasePath(dbname).getAbsolutePath());
        } catch (Exception e) {
            Log.e(SQLitePlugin.class.getSimpleName(), "couldn't delete database", e);
            return false;
        }
    }
}
