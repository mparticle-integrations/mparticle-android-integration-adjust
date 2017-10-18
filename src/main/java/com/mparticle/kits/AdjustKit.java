package com.mparticle.kits;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustReferrerReceiver;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.mparticle.AttributionError;
import com.mparticle.AttributionResult;
import com.mparticle.MParticle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Embedded implementation of the Adjust SDK
 * <p/>
 */
public class AdjustKit extends KitIntegration implements OnAttributionChangedListener, Application.ActivityLifecycleCallbacks {

    private static final String APP_TOKEN = "appToken";

    @Override
    public Object getInstance() {
        return Adjust.getDefaultInstance();
    }

    @Override
    public String getName() {
        return "Adjust";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        boolean production = MParticle.Environment.Production.equals(MParticle.getInstance().getEnvironment());

        AdjustConfig config = new AdjustConfig(getContext(),
                getSettings().get(APP_TOKEN),
                production ? AdjustConfig.ENVIRONMENT_PRODUCTION : AdjustConfig.ENVIRONMENT_SANDBOX);

        config.setOnAttributionChangedListener(this);

        if (!production){
            config.setLogLevel(LogLevel.VERBOSE);
        }
        config.setEventBufferingEnabled(false);
        Adjust.onCreate(config);
        ((Application)context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
        return null;
    }

    @Override
    public void setInstallReferrer(Intent intent) {
        new AdjustReferrerReceiver().onReceive(getContext(), intent);
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optOutStatus) {
        Adjust.setEnabled(!optOutStatus);
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(
                new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null)
                .setOptOut(optOutStatus)
        );
        return messageList;
    }

    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {
        // if Attribution has not been fetch yet the argument
        // will be null, in this case we should do nothing and wait for
        // the asynchronous callback to return
        if (attribution == null) return;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = toJSON(attribution);
        } catch (JSONException e) {
            AttributionError error = new AttributionError()
                    .setMessage(e.getMessage())
                    .setServiceProviderId(MParticle.ServiceProviders.ADJUST);
            getKitManager().onError(error);
        }
        AttributionResult deepLinkResult = new AttributionResult()
                .setParameters(jsonObject)
                .setServiceProviderId(MParticle.ServiceProviders.ADJUST);
        getKitManager().onResult(deepLinkResult);
    }

    public static JSONObject toJSON(AdjustAttribution attribution) throws JSONException {
        return new JSONObject()
                .putOpt("tracker_token", attribution.trackerToken)
                .putOpt("tracker_name", attribution.trackerName)
                .putOpt("network", attribution.network)
                .putOpt("campaign", attribution.campaign)
                .putOpt("adgroup", attribution.adgroup)
                .putOpt("creative", attribution.creative)
                .putOpt("click_label", attribution.clickLabel)
                .putOpt("adid", attribution.adid);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Adjust.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Adjust.onPause();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}