package com.logkit.core

import android.util.Log
import java.util.Locale

/**
 * Blocks or allows logs based on presence of sensitive patterns.
 *
 * In debug/QA (isSensitiveAllowed = true): allows but warns.
 * In release   (isSensitiveAllowed = false): blocks anything matching keywords.
 */
class DefaultLogGuard : LogGuard {

    // 1) Vehicle / hardware identification (direct IDs)
    private val vehicleIdKeywords = arrayOf(
        "vin",
        "vehicle_id", "vehicleid",
        "chassis_id", "chassisid",
        "ecu", "tcu", "bcu",
        "hwid", "hw_id", "hw-id",
        "serial", "serialno", "serial_no", "sn",
        "imei", "meid"
    )

    // 2) Vehicle property / VHAL / CarProperty references
    private val vehiclePropertyKeywords = arrayOf(
        "vehiclepropertyids", "vehicle_property_ids",
        "vehicle_propertyid", "vehicle_property_id",
        "vehicle_property", "vehicleproperty",
        "vhal", "vehicle_hal", "vehiclehal",
        "carpropertymanager", "car_property_manager",
        "carproperty", "car_property",
        "vehicle_property::", "vehicleproperty::",
        "vehicle_prop_", "veh_prop_", "vehproperty",
        "ev_battery", "battery_soc", "ev_soc",
        "odometer", "odo",
        "range_km", "remaining_range", "distance_to_empty",
        "fuel_level", "fuel_level_low"
    )

    // 3) Location / trip
    private val locationKeywords = arrayOf(
        "gps",
        "lat=", "lon=", "lng=",
        "latitude", "longitude",
        "location=", "location ",
        "route=", "route ",
        "trip_id", "tripid",
        "home_location", "work_location"
    )

    // 4) PII
    private val piiKeywords = arrayOf(
        "email", "mail=",
        "phone", "phonenumber", "phone_number",
        "user_name", "username", "name=",
        "profileid", "profile_id",
        "userid", "user_id",
        "contact", "contacts",
        "bt_mac", "bluetooth_mac"
    )

    // 5) Auth / session
    private val authKeywords = arrayOf(
        "authorization", "authorisation",
        "bearer ",
        "access_token", "accesstoken",
        "refresh_token", "refreshtoken",
        "id_token", "idtoken",
        "token=", "token ",
        "sessionid", "session_id",
        "password", "passwd", "pwd",
        "pin=", "pin "
    )

    // 6) Crypto / keys
    private val cryptoKeywords = arrayOf(
        "private_key", "privatekey",
        "public_key", "publickey",
        "secret_key", "secretkey",
        "certificate", "cert_pem",
        "-----begin certificate",
        "-----begin private key",
        "nonce", "hmac", "signature"
    )

    // 7) Internal URLs / services
    private val urlKeywords = arrayOf(
        "https://api.",
        "http://api.",
        "://int.", "://internal.", "://staging.", "://dev.",
        "telematics", "ota_update"
    )

    private val allCategories = arrayOf(
        vehicleIdKeywords,
        vehiclePropertyKeywords,
        locationKeywords,
        piiKeywords,
        authKeywords,
        cryptoKeywords,
        urlKeywords
    )

    override fun filter(priority: Int, tag: String, message: String): String? {
        val lower = message.lowercase(Locale.US)

        var containsSensitive = false
        outer@ for (category in allCategories) {
            for (keyword in category) {
                if (lower.contains(keyword)) {
                    containsSensitive = true
                    break@outer
                }
            }
        }

        if (!containsSensitive) {
            return message
        }

        return if (LogKit.isSensitiveAllowed()) {
            // debug/QA: allow, but warn so devs see it
            if (LogKit.isDebugBuild()) {
                Log.w(
                    "LogKitGuard",
                    "Sensitive log ALLOWED in this build. tag=$tag, priority=$priority"
                )
            }
            message
        } else {
            // release: block
            if (LogKit.isDebugBuild()) {
                Log.w(
                    "LogKitGuard",
                    "Blocked sensitive log. tag=$tag, priority=$priority"
                )
            }
            null
        }
    }
}
