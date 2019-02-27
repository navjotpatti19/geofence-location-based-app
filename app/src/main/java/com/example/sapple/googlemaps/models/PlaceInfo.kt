package com.example.sapple.googlemaps.models

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

data class PlaceInfo(var name: String?,var address: String?,
                     var phone: String?,var id: String?,
                     var websiteUri: Uri?, var latLng: LatLng?,
                     var rating: Float?, var attributions: String?)