package com.mparticle.kits

import android.content.Context
import com.adjust.sdk.AdjustAttribution
import com.mparticle.kits.AdjustKit.Companion.toJSON
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class AdjustKitTests {
    private val kit: KitIntegration
         get() = AdjustKit()

    @Test
    @Throws(Exception::class)
    fun testGetName() {
        val name = kit.name
        Assert.assertTrue(!name.isNullOrEmpty())
    }

    /**
     * Kit *should* throw an exception when they're initialized with the wrong settings.
     *
     */
    @Test
    @Throws(Exception::class)
    fun testOnKitCreate() {
        var e: Exception? = null
        try {
            val kit = kit
            val settings = HashMap<String, String>()
            settings["fake setting"] = "fake"
            kit.onKitCreate(settings, Mockito.mock(Context::class.java))
        } catch (ex: Exception) {
            e = ex
        }
        Assert.assertNotNull(e)
    }

    @Test
    @Throws(Exception::class)
    fun testClassName() {
        val factory = KitIntegrationFactory()
        val integrations = factory.knownIntegrations
        val className = kit.javaClass.name
        for (integration in integrations) {
            if (integration.value == className) {
                return
            }
        }
        Assert.fail("$className not found as a known integration.")
    }

    @Test
    @Throws(JSONException::class)
    fun testAttributionToJSON() {
        val originalAttributionJSON = attributionJSON
        val attribution = AdjustAttribution.fromJson(
            originalAttributionJSON,
            originalAttributionJSON.getString("adid"),
            "android"
        )
        val attributionJSON = toJSON(attribution)
        Assert.assertEquals(originalAttributionJSON.toString(), attributionJSON.toString())
    }

    @get:Throws(JSONException::class)
    private val attributionJSON: JSONObject
         get() {
            val jsonObject = JSONObject()
            jsonObject.putOpt("tracker_token", "a1")
            jsonObject.putOpt("tracker_name", "b2")
            jsonObject.putOpt("network", "c3")
            jsonObject.putOpt("campaign", "d4")
            jsonObject.putOpt("adgroup", "e5")
            jsonObject.putOpt("creative", "f6")
            jsonObject.putOpt("click_label", "g7")
            jsonObject.putOpt("adid", "h8")
            return jsonObject
        }
}