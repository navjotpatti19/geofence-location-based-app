package com.example.sapple.googlemaps.activities

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.example.sapple.googlemaps.entities.ReminderData
import com.example.sapple.googlemaps.receivers.GeoFenceReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class ReminderRepository(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeoFenceReceiver::class.java)
        intent.action = "com.example.sapple.googlemaps.ACTION_RECEIVE_GEOFENCE"
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun add(reminder: ReminderData,
        success: () -> Unit,
        failure: (error: String) -> Unit) {
        val geofence = buildGeofence(reminder)
        if (geofence != null && ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            geofencingClient.addGeofences(buildGeofencingRequest(geofence), getPendingIntent())
                    .addOnSuccessListener {
                        success()
                    }
                    .addOnFailureListener {
                        failure(GeofenceErrorMessages.getErrorString(context, it))
                    }
        }
    }

    private fun buildGeofence(reminder: ReminderData): Geofence? {
        val latitude = reminder.latitude
        val longitude = reminder.longitude
        val radius = reminder.distance

        if (latitude != null && longitude != null && radius != null) {
            val geofence: Geofence
            if(reminder.inOrOutFlag == 0) {
                geofence = Geofence.Builder()
                        .setRequestId(reminder.primaryId.toString())
                        .setCircularRegion(
                                latitude.toDouble(),
                                longitude.toDouble(),
                                radius.toFloat()
                        )
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build()
            } else {
                geofence = Geofence.Builder()
                        .setRequestId(reminder.primaryId.toString())
                        .setCircularRegion(
                                latitude.toDouble(),
                                longitude.toDouble(),
                                radius.toFloat()
                        )
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build()
            }
            return geofence
        }
        return null
    }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    fun remove(reminder: ReminderData,
                 success: () -> Unit,
                 failure: (error: String) -> Unit) {
        geofencingClient.removeGeofences(listOf(reminder.primaryId.toString()))
            .addOnSuccessListener {
              success()
            }
            .addOnFailureListener {
              failure(GeofenceErrorMessages.getErrorString(context, it))
            }
    }
}