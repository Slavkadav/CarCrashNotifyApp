package ru.bstu.vt41.davydov.vkr.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ru.bstu.vt41.davydov.vkr.R


class NotifyService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val injured = intent?.extras?.getBoolean(getString(R.string.is_injured))
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        generateMessage()

        return START_STICKY

    }


    private fun generateMessage() {
        var longitude: Double? = null
        var latitude: Double? = null
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            TODO()
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                longitude = location?.longitude
                latitude = location?.latitude
                Toast.makeText(baseContext, latitude.toString(), Toast.LENGTH_LONG).show()
                val userName = getUserName()
                val messageTemplate = getMessageTemplate()

                val text = "$userName попал в дтп по месту $latitude $longitude"

                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage("+79997003661", null, text, null, null)
                Toast.makeText(applicationContext, "SMS отправлено", Toast.LENGTH_LONG).show()

                stopSelf()
            }
        }

    }

    private fun getUserName(): String {
      return PreferenceManager.getDefaultSharedPreferences(this).getString("username", "")
    }

    private fun getMessageTemplate(): String {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("sms_template","")
    }

    private fun getContacts(){
        TODO()
    }
}
