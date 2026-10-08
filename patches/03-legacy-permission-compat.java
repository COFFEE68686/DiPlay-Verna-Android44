package com.shilapi.xcertplay.compat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public final class LegacyPermissionCompat {
    private static final String TAG = "LegacyPermissionCompat";

    private LegacyPermissionCompat() {
    }

    public static boolean hasPermission(Context context, String permission) {
        // Android 4.4.4 does not use the modern runtime permission flow.
        // If the app is installed, the permission is considered granted at install time.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissionIfNeeded(Activity activity, String permission, int requestCode) {
        // Legacy-safe behavior: skip runtime prompt for old devices.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Skipping runtime permission request on legacy API: " + permission);
            return;
        }

        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[] { permission }, requestCode);
        }
    }
}
```

Use this helper instead of directly calling `requestPermissions()` in the app.
This prevents crashes on Android 4.4.4 and avoids unsupported permission logic.
