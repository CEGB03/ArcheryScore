package com.cegb03.archeryscore.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location? {
    val client = LocationServices.getFusedLocationProviderClient(context)

    return suspendCancellableCoroutine { continuation ->
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    val token = CancellationTokenSource()
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
                        .addOnSuccessListener { current ->
                            continuation.resume(current)
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
