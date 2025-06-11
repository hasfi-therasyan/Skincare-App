package com.skincare.apitest.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.skincare.apitest.R
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.Reseller
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class ResellerMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var viewModel: ProductViewModel
    private var userMarker: Marker? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableUserLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reseller_map)

        viewModel = ProductViewModel()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // TODO: Setup search bar and observe resellers data
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableUserLocation()
        loadResellers()
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun loadResellers() {
        lifecycleScope.launch {
            viewModel.fetchResellers()
            viewModel.resellersState.collect { response ->
                when (response) {
                    is ApiResponse.Loading -> { }
                    is ApiResponse.Success -> {
                        response.data.forEach { reseller ->
                            addResellerMarker(reseller)
                        }
                    }
                    is ApiResponse.Error -> {
                        Toast.makeText(this@ResellerMapActivity, "Failed to load resellers", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addResellerMarker(reseller: Reseller) {
        val position = LatLng(reseller.latitude, reseller.longitude)
        val markerOptions = MarkerOptions()
            .position(position)
            .title(reseller.shopName)
            .snippet("Reseller: ${reseller.resellerName}\\nCity: ${reseller.city}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        map.addMarker(markerOptions)
    }

    private fun searchProvince(provinceName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(provinceName, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val location = addresses[0]
            val latLng = LatLng(location.latitude, location.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        } else {
            Toast.makeText(this, "Province not found", Toast.LENGTH_SHORT).show()
        }
    }
}