package com.example.sapple.googlemaps.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.sapple.googlemaps.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(var context: Context): GoogleMap.InfoWindowAdapter {

    private val window: View = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)

    private fun renderWindowText(marker: Marker, view: View) {
        val title = marker.title
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)

        if(title != "") {
            tvTitle.text = title
        }

        val snippet = marker.snippet
        val tvSnippet = view.findViewById<TextView>(R.id.tvSnippet)

        if(snippet != "") {
            tvSnippet.text = snippet
        }
    }

    override fun getInfoContents(marker: Marker?): View {
        renderWindowText(marker!!, window)
        return window
    }

    override fun getInfoWindow(marker: Marker?): View {
        renderWindowText(marker!!, window)
        return window
    }
}