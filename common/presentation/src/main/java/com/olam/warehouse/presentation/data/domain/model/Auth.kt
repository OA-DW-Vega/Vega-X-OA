package com.olam.warehouse.presentation.data.domain.model

import com.olam.warehouse.presentation.utils.UIUtils.decodeTokenGetKeyCloakId

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
data class Auth(
    val access_token: String,
    val token_type: String,
    val refresh_token: String,
    val expires_in: Int,
    val scope: String,
    val jti: String? = ""
)

data class LoginInfo(
    val username: String,
    val password: String,
    val grant_type: String
)

data class LoginWrapper(val data: LoginInfo)

data class TruckManagementVehicleResponse(
    var vehicleId:String,
    var vehicleList : List<VehicleData>
)
data class TruckManagementSeasonResponse(
    var statusCode:String,
    var statusMessage:String,
    var seasonList:List<TruckManagementData>
)
data class TruckManagementData(
    var seasonID:String,
    var startingPeriod:String,
    var endingPeriod:String,
    var region:String,
    var status:String
)

data class VehicleData(
    var vehicleId: String,
    var qrCodeNumber: String,
    var vehicleNumber: String,
    var driverDetails: List<DriverData>,
    var vendor: Vendor,
    var typeOfVehicle: String,
    var status: String,
    var transportOfficerName: String,
)

data class DriverData(
    var driverId :String,
    var driverName :String,
    var driverPhone :String,
    var driverLicenseNumber :String,
)

data class Vendor(
    var vendorCode :String,
    var name :String
)

data class ResetPasswordModel(
    var type :String = "password",
    var value :String= "",
    var temporary :Boolean = false
)

data class ResetPasswordNewModel(
    var username :String= "",
    var password :String= "",
    var userid :String= decodeTokenGetKeyCloakId(),
    var enabled :Boolean = true,
    var isTemporaryPassword :Boolean = false,
)
