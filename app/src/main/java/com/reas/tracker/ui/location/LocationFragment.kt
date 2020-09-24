package com.reas.tracker.ui.location

import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.reas.tracker.R
import com.reas.tracker.service.location.LocationService
import kotlinx.android.synthetic.main.fragment_location.*


class LocationFragment : Fragment() {
    private var fusedLocationFragment: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest = LocationRequest.create()
    private var mLocationCallback: LocationCallback? = null


    private var lastLocation: Location? = null

    private var textView2: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationFragment = LocationServices.getFusedLocationProviderClient(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_location, container, false)
//        textView2 = root.findViewById(R.id.textView2)
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test.text = "${Build.DISPLAY} \n ${Build.DEVICE} \n ${Build.BOARD} \n ${Build.BOOTLOADER} \n ${Build.BRAND} \n ${Build.FINGERPRINT} \n ${Build.HARDWARE} \n ${Build.HOST} \n ${Build.ID} \n ${Build.MANUFACTURER} \n ${Build.MODEL} \n ${Build.PRODUCT} \n ${Build.TAGS} \n  ${Build.TIME} \n ${Build.TYPE} \n ${Build.UNKNOWN} \n ${Build.USER} \n"


        button2.setOnClickListener {
            val locationService = Intent(context, LocationService::class.java)
            locationService.addFlags(Intent.FLAG_FROM_BACKGROUND)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(locationService)
            } else {
                requireContext().startService(locationService)
            }
        }


    }



}