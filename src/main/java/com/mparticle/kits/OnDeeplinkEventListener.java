package com.mparticle.kits;

import android.net.Uri;

public interface OnDeeplinkEventListener {
    boolean launchReceivedDeeplink(Uri deeplink);
}
