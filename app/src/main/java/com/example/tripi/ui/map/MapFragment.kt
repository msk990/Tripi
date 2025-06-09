package com.example.tripi.ui.map  // Adjust the package name to match your project

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.tripi.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.graphics.scale
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.BitmapDescriptor

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()

        // Add 3 sample markers
        val markers = loadMarkersFromJson(requireContext())

        val fallbackResId = R.drawable.ic_marker_default // 👈 create a safe default icon in drawable
        fun resizeMarkerIcon(resourceId: Int, width: Int, height: Int, context: Context): BitmapDescriptor {
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            val scaledBitmap = bitmap.scale(width, height, false)
            return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        }
        for (marker in markers) {
            @Suppress("DiscouragedApi")
            val resId = resources.getIdentifier(marker.icon, "drawable", requireContext().packageName)

            val validResId = if (resId != 0) resId else fallbackResId
            if (resId == 0) {
                Log.w("MapFragment", "⚠️ Missing icon: '${marker.icon}', using fallback.")
            }

            val icon = resizeMarkerIcon(validResId, 100, 100, requireContext())

            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(marker.lat, marker.lng))
                    .title(marker.title)
                    .icon(icon)
            )
        }
        googleMap.setOnMarkerClickListener { marker ->
            val action = MapFragmentDirections.actionNavigationMapToArFragment(marker.title ?: "Unknown")
            findNavController().navigate(action)
            true  // return true to consume the event
        }


    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            googleMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val myLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                }
            }

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }
    private fun loadMarkersFromJson(context: Context): List<MapMarker> {
        val inputStream = context.resources.openRawResource(R.raw.markers)
        val json = inputStream.bufferedReader().use { it.readText() }
        val gson = Gson()
        val type = object : TypeToken<List<MapMarker>>() {}.type
        return gson.fromJson(json, type)
    }

}
