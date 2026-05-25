/**
 * Created by SangiliPandian C on 06-11-2019.
 */
object AndroidConfig {
    const val COMPILE_SDK_VERSION = 34
    const val MIN_SDK_VERSION = 24
    const val TARGET_SDK_VERSION = 34
    const val BUILD_TOOLS_VERSION = "34.0.0"

    const val VERSION_CODE = 2
    const val VERSION_NAME = "1.1"

    const val ID = "com.olam.warehouse.vegax"
    const val ID_UAT = ".uat"
    const val TEST_INSTRUMENTATION_RUNNER = "android.support.test.runner.AndroidJUnitRunner"
}

interface BuildType {
    companion object {
        const val RELEASE = "release"
        const val DEBUG = "debug"
        const val SIT = "sit"
        const val UAT = "uat"
        const val DEMO = "demo"
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
    var entity = ""
    const val type = "String"
    const val baseUrl = "BASE_URL"
    const val baseDoUrl = "BASE_DO_URL"
    const val baseTruckUrl = "BASE_TRUCK_URL"
    const val currentOrigin = "CURRENT_ORIGIN"
    const val versionName = "VERSION_NAME"
    const val versionCode = "VERSION_CODE"

    //Keycloak
    const val baseUrlKey = "BASE_URL_KEY"
    const val baseUrlKeyResetPass = "BASE_URL_KEY_RESET_PASS"
    const val keyClientId = "KEY_CLIENT_ID"
    const val keyClientSecret = "KEY_CLIENT_SECRET"



    const val dev = "\"https://vega-uat.olamagri.com/\""
    const val sit = "\"https://vega-sit.olamagri.com/\""
    const val uat = "\"https://vega-uat.olamagri.com/\""
    const val prod = "\"https://vega.olamagri.com/\""
    const val demo = "\"https://vega-api-demo.olamdigital.com/\""

    const val doDev = "\"https://dev-apigateway.olamdirect.com/\""
    const val doSit = "\"https://sit-apigateway.olamdirect.com/\""
    const val doUat = "\"https://uat-apigateway-global.olamdirect.com/\""
    const val doProd = "\"https://app-apigateway.olamdirect.com/\""

    const val devBaseKeycloakUrl =
        "\"https://authdev.olamagri.com/auth/realms/vega-manna-uat/protocol/openid-connect/\""
    const val devBaseKeycloakResetPassUrl = "\"https://digitalauthdev.olamnet.com/auth/admin/realms/vega-manna-uat/users/\""
    const val devKeyClientId = "\"mobile-vega-uat\""
    const val devKeyClientSecret = "\"f7373fc4-6f41-4ff6-ab60-b3ac56efea10\""
    const val devKeyClientId_oga = "\"oga-mobile\""
    const val devKeyClientSecret_oga = "\"5Jbx5Oaog0NFsTa8eqSmqP8jbroQ17nY\""

  /*  const val devBaseKeycloakUrl =
        "\"https://digitalauthdev.olamnet.com/auth/realms/vega-manna-dev/protocol/openid-connect/\""
    const val devBaseKeycloakResetPassUrl = "\"https://digitalauthdev.olamnet.com/auth/admin/realms/vega-manna-dev/users/\""
    const val devKeyClientId = "\"mobile-vega-dev\""
    const val devKeyClientSecret = "\"4e4b179c-4072-4c1f-b89b-42ac24608fa5\""
    const val devKeyClientId_oga = "\"oga-mobile\""
    const val devKeyClientSecret_oga = "\"xXk2auDFoyj1YqPQRK8eXFpbxMMa2Qdy\""*/


    const val sitBaseKeycloakUrl =
        "\"https://authdev.olamagri.com/auth/realms/vega-manna-sit/protocol/openid-connect/\""
    const val sitBaseKeycloakResetPassUrl = "\"https://digitalauthdev.olamnet.com/auth/admin/realms/vega-manna-sit/users/\""
    const val sitKeyClientId = "\"mobile-vega-sit\""
    const val sitKeyClientSecret = "\"20af39cd-cc0c-4e5d-96da-bd2f01d45af5\""
    const val sitKeyClientId_oga = "\"oga-mobile\""
    const val sitKeyClientSecret_oga = "\"FNIrE52FqWWyd6g4mJ03YmYhPw0MYUsG\""

    const val uatBaseKeycloakUrl =
        "\"https://authdev.olamagri.com/auth/realms/vega-manna-uat/protocol/openid-connect/\""
    const val uatBaseKeycloakResetPassUrl = "\"https://digitalauthdev.olamnet.com/auth/admin/realms/vega-manna-uat/users/\""
    const val uatKeyClientId = "\"mobile-vega-uat\""
    const val uatKeyClientSecret = "\"f7373fc4-6f41-4ff6-ab60-b3ac56efea10\""
    const val uatKeyClientId_oga = "\"oga-mobile\""
    const val uatKeyClientSecret_oga = "\"5Jbx5Oaog0NFsTa8eqSmqP8jbroQ17nY\""

    const val prodBaseKeycloakUrl =
        "\"https://digitalauth.olamnet.com/auth/realms/vega/protocol/openid-connect/\""
    const val prodBaseKeycloakResetPassUrl = "\"https://digitalauthdev.olamnet.com/auth/admin/realms/vega/users/\""
    const val prodKeyClientId = "\"mobile-vega\""
    const val prodKeyClientSecret = "\"921594ca-e351-4580-90e5-83f8c690468f\""
    const val prodKeyClientId_oga = "\"oga-mobile\""
    const val prodKeyClientSecret_oga = "\"ifNYElC8m3khAsqj8dRK2GSm8XBj0VLa\""

    const val truckDev = "\"https://pixel-gateway-sit.ofighanadigital.com/\""
    const val truckSit = "\"https://pixel-gateway-sit.ofighanadigital.com/\""
    const val truckUat = "\"https://pixel-gateway-sit.ofighanadigital.com/\""
    const val truckProd = "\"https://pixel-gateway-prod.ofighanadigital.com/\""
}
