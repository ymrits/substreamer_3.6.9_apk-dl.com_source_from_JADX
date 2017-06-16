package android.support.v4.content.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.BuildCompat;
import android.text.TextUtils;

public class ShortcutManagerCompat {
    @VisibleForTesting
    static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    @VisibleForTesting
    static final String INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.permission.INSTALL_SHORTCUT";

    /* renamed from: android.support.v4.content.pm.ShortcutManagerCompat.1 */
    static class C00241 extends BroadcastReceiver {
        final /* synthetic */ IntentSender val$callback;

        C00241(IntentSender intentSender) {
            this.val$callback = intentSender;
        }

        public void onReceive(Context context, Intent intent) {
            try {
                this.val$callback.sendIntent(context, 0, null, null, null);
            } catch (SendIntentException e) {
            }
        }
    }

    public static boolean isRequestPinShortcutSupported(@NonNull Context context) {
        if (BuildCompat.isAtLeastO()) {
            return ShortcutManagerCompatApi26.isRequestPinShortcutSupported(context);
        }
        if (ContextCompat.checkSelfPermission(context, INSTALL_SHORTCUT_PERMISSION) != 0) {
            return false;
        }
        for (ResolveInfo info : context.getPackageManager().queryBroadcastReceivers(new Intent(ACTION_INSTALL_SHORTCUT), 0)) {
            String permission = info.activityInfo.permission;
            if (!TextUtils.isEmpty(permission)) {
                if (INSTALL_SHORTCUT_PERMISSION.equals(permission)) {
                }
            }
            return true;
        }
        return false;
    }

    public static boolean requestPinShortcut(@NonNull Context context, @NonNull ShortcutInfoCompat shortcut, @Nullable IntentSender callback) {
        if (BuildCompat.isAtLeastO()) {
            return ShortcutManagerCompatApi26.requestPinShortcut(context, shortcut, callback);
        }
        if (!isRequestPinShortcutSupported(context)) {
            return false;
        }
        Intent intent = shortcut.addToIntent(new Intent(ACTION_INSTALL_SHORTCUT));
        if (callback == null) {
            context.sendBroadcast(intent);
            return true;
        }
        context.sendOrderedBroadcast(intent, null, new C00241(callback), null, -1, null, null);
        return true;
    }

    @NonNull
    public static Intent createShortcutResultIntent(@NonNull Context context, @NonNull ShortcutInfoCompat shortcut) {
        Intent result = null;
        if (BuildCompat.isAtLeastO()) {
            result = ShortcutManagerCompatApi26.createShortcutResultIntent(context, shortcut);
        }
        if (result == null) {
            result = new Intent();
        }
        return shortcut.addToIntent(result);
    }
}
