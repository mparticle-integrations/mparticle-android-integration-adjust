package com.mparticle.kits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustReferrerReceiver;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.mparticle.DeepLinkError;
import com.mparticle.DeepLinkResult;
import com.mparticle.MParticle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p/>
 * Embedded implementation of the Adjust SDK
 * <p/>
 */
public class AdjustKit extends KitIntegration implements KitIntegration.ActivityListener, OnAttributionChangedListener {

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
        return null;
    }

    @Override
    public void setInstallReferrer(Intent intent) {
        new AdjustReferrerReceiver().onReceive(getContext(), intent);
    }

    @Override
    public List<ReportingMessage> onActivityCreated(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStarted(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityResumed(Activity activity) {
        Adjust.onResume();
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(
            new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null)
        );
        return messageList;
    }

    @Override
    public List<ReportingMessage> onActivityPaused(Activity activity) {
        Adjust.onPause();
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(
                new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null)
        );
        return messageList;
    }

    @Override
    public List<ReportingMessage> onActivityStopped(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityDestroyed(Activity activity) {
        return null;
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
    public void checkForDeepLink() {
        onAttributionChanged(Adjust.getAttribution());
    }

    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {
        // if Attribution has not been fetch yet, the value passed in checkForDeepLink(), from
        // Adjust.getAttribution() will be null, in this case we should do nothing and wait for
        // the asynchronous callback to return
        if (attribution == null) return;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = toJSON(attribution);
        } catch (JSONException e) {
            DeepLinkError error = new DeepLinkError()
                    .setMessage(e.getMessage())
                    .setServiceProviderId(MParticle.ServiceProviders.ADJUST);
            getKitManager().onError(error);
        }
        DeepLinkResult deepLinkResult = new DeepLinkResult()
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
}