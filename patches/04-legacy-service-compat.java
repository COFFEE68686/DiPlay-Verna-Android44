package com.shilapi.xcertplay.compat;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public final class LegacyServiceCompat {
    private static final String TAG = "LegacyServiceCompat";

    private LegacyServiceCompat() {
    }

    public static void startServiceCompat(Service service, Intent intent) {
        if (intent == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Modern Android requires foreground service handling.
            // Legacy Android 4.4.4 does not have this path and should use the older form.
            Log.d(TAG, "Using modern startService path for API 26+.");
        }

        // Android 4.4.4 and older: keep the simple service launch path.
        service.startService(intent);
    }
}
```

This is a compatibility shim for any service startup path that would normally call
`startForegroundService()` on modern Android devices.

For a minimal Verna port, the service should not depend on foreground-service-only behavior.
