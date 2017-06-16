package org.apache.cordova;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.widget.EditText;

public class CordovaDialogsHelper {
    private final Context context;
    private AlertDialog lastHandledDialog;

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.1 */
    class C01921 implements OnClickListener {
        final /* synthetic */ Result val$result;

        C01921(Result result) {
            this.val$result = result;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$result.gotResult(true, null);
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.2 */
    class C01932 implements OnCancelListener {
        final /* synthetic */ Result val$result;

        C01932(Result result) {
            this.val$result = result;
        }

        public void onCancel(DialogInterface dialog) {
            this.val$result.gotResult(false, null);
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.3 */
    class C01943 implements OnKeyListener {
        final /* synthetic */ Result val$result;

        C01943(Result result) {
            this.val$result = result;
        }

        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode != 4) {
                return true;
            }
            this.val$result.gotResult(true, null);
            return false;
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.4 */
    class C01954 implements OnClickListener {
        final /* synthetic */ Result val$result;

        C01954(Result result) {
            this.val$result = result;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$result.gotResult(true, null);
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.5 */
    class C01965 implements OnClickListener {
        final /* synthetic */ Result val$result;

        C01965(Result result) {
            this.val$result = result;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$result.gotResult(false, null);
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.6 */
    class C01976 implements OnCancelListener {
        final /* synthetic */ Result val$result;

        C01976(Result result) {
            this.val$result = result;
        }

        public void onCancel(DialogInterface dialog) {
            this.val$result.gotResult(false, null);
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.7 */
    class C01987 implements OnKeyListener {
        final /* synthetic */ Result val$result;

        C01987(Result result) {
            this.val$result = result;
        }

        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode != 4) {
                return true;
            }
            this.val$result.gotResult(false, null);
            return false;
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.8 */
    class C01998 implements OnClickListener {
        final /* synthetic */ EditText val$input;
        final /* synthetic */ Result val$result;

        C01998(EditText editText, Result result) {
            this.val$input = editText;
            this.val$result = result;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$result.gotResult(true, this.val$input.getText().toString());
        }
    }

    /* renamed from: org.apache.cordova.CordovaDialogsHelper.9 */
    class C02009 implements OnClickListener {
        final /* synthetic */ Result val$result;

        C02009(Result result) {
            this.val$result = result;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$result.gotResult(false, null);
        }
    }

    public interface Result {
        void gotResult(boolean z, String str);
    }

    public CordovaDialogsHelper(Context context) {
        this.context = context;
    }

    public void showAlert(String message, Result result) {
        Builder dlg = new Builder(this.context);
        dlg.setMessage(message);
        dlg.setTitle("Alert");
        dlg.setCancelable(true);
        dlg.setPositiveButton(17039370, new C01921(result));
        dlg.setOnCancelListener(new C01932(result));
        dlg.setOnKeyListener(new C01943(result));
        this.lastHandledDialog = dlg.show();
    }

    public void showConfirm(String message, Result result) {
        Builder dlg = new Builder(this.context);
        dlg.setMessage(message);
        dlg.setTitle("Confirm");
        dlg.setCancelable(true);
        dlg.setPositiveButton(17039370, new C01954(result));
        dlg.setNegativeButton(17039360, new C01965(result));
        dlg.setOnCancelListener(new C01976(result));
        dlg.setOnKeyListener(new C01987(result));
        this.lastHandledDialog = dlg.show();
    }

    public void showPrompt(String message, String defaultValue, Result result) {
        Builder dlg = new Builder(this.context);
        dlg.setMessage(message);
        EditText input = new EditText(this.context);
        if (defaultValue != null) {
            input.setText(defaultValue);
        }
        dlg.setView(input);
        dlg.setCancelable(false);
        dlg.setPositiveButton(17039370, new C01998(input, result));
        dlg.setNegativeButton(17039360, new C02009(result));
        this.lastHandledDialog = dlg.show();
    }

    public void destroyLastDialog() {
        if (this.lastHandledDialog != null) {
            this.lastHandledDialog.cancel();
        }
    }
}
