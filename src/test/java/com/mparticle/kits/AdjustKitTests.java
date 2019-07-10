package com.mparticle.kits;


import android.content.Context;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AdjustKitTests {

    public AdjustKitTests() throws JSONException {
    }

    private KitIntegration getKit() {
        return new AdjustKit();
    }

    @Test
    public void testGetName() throws Exception {
        String name = getKit().getName();
        assertTrue(name != null && name.length() > 0);
    }

    /**
     * Kit *should* throw an exception when they're initialized with the wrong settings.
     *
     */
    @Test
    public void testOnKitCreate() throws Exception{
        Exception e = null;
        try {
            KitIntegration kit = getKit();
            Map settings = new HashMap<>();
            settings.put("fake setting", "fake");
            kit.onKitCreate(settings, Mockito.mock(Context.class));
        }catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testClassName() throws Exception {
        KitIntegrationFactory factory = new KitIntegrationFactory();
        Map<Integer, String> integrations = factory.getKnownIntegrations();
        String className = getKit().getClass().getName();
        for (Map.Entry<Integer, String> entry : integrations.entrySet()) {
            if (entry.getValue().equals(className)) {
                return;
            }
        }
        fail(className + " not found as a known integration.");
    }

    @Test
    public void testAttributionToJSON() throws JSONException {
        JSONObject originalAttributionJSON = getAttributionJSON();
        AdjustAttribution attribution = AdjustAttribution.fromJson(originalAttributionJSON, originalAttributionJSON.getString("adid"), "android");
        JSONObject attributionJSON = AdjustKit.toJSON(attribution);
        assertEquals(originalAttributionJSON.toString(), attributionJSON.toString());
    }

    private JSONObject getAttributionJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("tracker_token", "a1");
        jsonObject.putOpt("tracker_name", "b2");
        jsonObject.putOpt("network", "c3");
        jsonObject.putOpt("campaign", "d4");
        jsonObject.putOpt("adgroup", "e5");
        jsonObject.putOpt("creative", "f6");
        jsonObject.putOpt("click_label", "g7");
        jsonObject.putOpt("adid", "h8");
        return jsonObject;
    }
}