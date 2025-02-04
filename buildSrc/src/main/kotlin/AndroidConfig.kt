/**
 * Created by SangiliPandian C on 06-11-2019.
 */
object AndroidConfig {
    const val COMPILE_SDK_VERSION = 29
    const val MIN_SDK_VERSION = 21
    const val TARGET_SDK_VERSION = 29
    const val BUILD_TOOLS_VERSION = "29.0.3"

    const val VERSION_CODE = 7
    const val VERSION_NAME = "1.7"

    const val ID = "com.olam.warehouse.vegax"
    const val TEST_INSTRUMENTATION_RUNNER = "android.support.test.runner.AndroidJUnitRunner"
}

interface BuildType {
    companion object {
        const val RELEASE = "release"
        const val DEBUG = "debug"
        const val SIT = "sit"
        const val UAT = "uat"
    }
    val isMinifyEnabled: Boolean
}

object BuildTypeDebug : BuildType {
    override val isMinifyEnabled = false
}

object BuildTypeRelease : BuildType {
    override val isMinifyEnabled = false
}

object TestOptions {
    const val IS_RETURN_DEFAULT_VALUES = true
}

object BuildConfig {
    const val type = "String"
    const val baseUrl = "BASE_URL"
    const val baseDoUrl = "BASE_DO_URL"
    const val baseTruckUrl = "BASE_TRUCK_URL"
    const val currentOrigin = "CURRENT_ORIGIN"

    //Keycloak
    const val baseUrlKey = "BASE_URL_KEY"
    const val keyClientId = "KEY_CLIENT_ID"
    const val keyClientSecret = "KEY_CLIENT_SECRET"

    //****************************Non Manna********************************************

//    const val dev = "\"https://vega-zuul-proxy-dev.olamdigital.com/\""
    /*const val dev = "\"https://vega-sit.olamdigital.com/\""
    const val sit = "\"https://vega-sit.olamdigital.com/\""
    const val uat = "\"https://vega-uat.olamdigital.com/\""
    const val prod = "\"https://vega-prod.olamdigital.com/\""

    const val doDev = "\"https://dev-apigateway.olamdirect.com/\""
    const val doSit = "\"https://sit-apigateway.olamdirect.com/\""
    const val doUat = "\"https://uat-apigateway-global.olamdirect.com/\""
    const val doProd = "\"https://app-apigateway.olamdirect.com/\""

    const val devBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-dev/protocol/openid-connect/\""
    const val devKeyClientId = "\"mobile-vega-dev\""
    const val devKeyClientSecret = "\"2e138169-0153-4663-a5f1-b6557fb26b8c\""

    const val sitBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-sit/protocol/openid-connect/\""
    const val sitKeyClientId = "\"mobile-vega-sit\""
    const val sitKeyClientSecret = "\"3a1c013c-e3f6-4a44-9503-921cdc894a19\""

    const val uatBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-uat/protocol/openid-connect/\""
    const val uatKeyClientId = "\"mobile-vega-uat\""
    const val uatKeyClientSecret = "\"16b30559-aea7-45c2-b71d-66d8569b090f\""

    const val prodBaseKeycloakUrl =
        "\"https://digitalauth.olamnet.com/auth/realms/vega/protocol/openid-connect/\""
    const val prodKeyClientId = "\"mobile-vega\""
    const val prodKeyClientSecret = "\"921594ca-e351-4580-90e5-83f8c690468f\""

    const val truckDev = "\"https://pixel-gateway-sit.olamghanadigital.com/\""
    const val truckSit = "\"https://pixel-gateway-sit.olamghanadigital.com/\""
    const val truckUat = "\"https://pixel-gateway-sit.olamghanadigital.com/\""
    const val truckProd = "\"https://pixel-gateway-prod.olamghanadigital.com/\""*/


    //**************************************Manna***************************************************

    const val dev = "\"https://vega-manna-uat.olamdigital.com/\""
    const val sit = "\"https://vega-manna-sit.olamdigital.com/\""
    const val uat = "\"https://vega-manna-uat.olamdigital.com/\""
    const val prod = "\"https://vega-prod.olamdigital.com/\""

    const val doDev = "\"https://dev-apigateway.olamdirect.com/\""
    const val doSit = "\"https://sit-apigateway.olamdirect.com/\""
    const val doUat = "\"https://uat-apigateway-global.olamdirect.com/\""
    const val doProd = "\"https://app-apigateway.olamdirect.com/\""

    /*const val devBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-manna-dev/protocol/openid-connect/\""
    const val devKeyClientId = "\"mobile-vega-dev\""
    const val devKeyClientSecret = "\"4e4b179c-4072-4c1f-b89b-42ac24608fa5\""*/

    /*const val devBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-manna-sit/protocol/openid-connect/\""
    const val devKeyClientId = "\"mobile-vega-sit\""
    const val devKeyClientSecret = "\"20af39cd-cc0c-4e5d-96da-bd2f01d45af5\""*/

    const val devBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-manna-uat/protocol/openid-connect/\""
    const val devKeyClientId = "\"mobile-vega-uat\""
    const val devKeyClientSecret = "\"f7373fc4-6f41-4ff6-ab60-b3ac56efea10\""


    const val sitBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-manna-sit/protocol/openid-connect/\""
    const val sitKeyClientId = "\"mobile-vega-sit\""
    const val sitKeyClientSecret = "\"20af39cd-cc0c-4e5d-96da-bd2f01d45af5\""

    const val uatBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-manna-uat/protocol/openid-connect/\""
    const val uatKeyClientId = "\"mobile-vega-uat\""
    const val uatKeyClientSecret = "\"f7373fc4-6f41-4ff6-ab60-b3ac56efea10\""

    const val prodBaseKeycloakUrl =
        "\"https://digitalauth.olamnet.com/auth/realms/vega/protocol/openid-connect/\""
    const val prodKeyClientId = "\"mobile-vega\""
    const val prodKeyClientSecret = "\"921594ca-e351-4580-90e5-83f8c690468f\""

    const val truckDev = "\"https://pixel-gateway-sit.olamghanadigital.com/\""
    const val truckSit = "\"https://pixel-gateway-sit.olamghanadigital.com/\""
    const val truckUat = "\"https://pixel-gateway-sit.olamghanadigital.com/\""
    const val truckProd = "\"https://pixel-gateway-prod.olamghanadigital.com/\""
}
