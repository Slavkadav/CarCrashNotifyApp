package ru.bstu.vt41.davydov.vkr.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import ru.bstu.vt41.davydov.vkr.R
import ru.bstu.vt41.davydov.vkr.service.AccelerationService


class MainActivity : SensorEventListener, AppCompatActivity() {
    private var accelerateXMaxValue = 0f
    private var accelerateYMaxValue = 0f
    private var accelerateZMaxValue = 0f
    private lateinit var mAccelerateX: TextView
    private lateinit var mAccelerateY: TextView
    private lateinit var mAccelerateZ: TextView

    private lateinit var mSensorAccelerate: Sensor
    private lateinit var mSensorManager: SensorManager
    private lateinit var mStatusTextView: TextView
    private lateinit var mSeriesX: LineGraphSeries<DataPoint>
    private lateinit var mSeriesY: LineGraphSeries<DataPoint>
    private lateinit var mSeriesZ: LineGraphSeries<DataPoint>
    private var mReadSensorsTrigger: Boolean = false
    private lateinit var mToggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mAccelerateX = findViewById(R.id.accelerateX)
        mAccelerateY = findViewById(R.id.accelerateY)
        mAccelerateZ = findViewById(R.id.accelerateZ)
        mStatusTextView = findViewById(R.id.status)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorAccelerate =
                if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                } else {
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                }
        mToggleButton = findViewById(R.id.button_toggle)
        val graphView: GraphView = findViewById(R.id.accelerate_chart)
        initGraph(graphView)
    }


    private fun initGraph(graphView: GraphView) {
        mSeriesX = LineGraphSeries()
        mSeriesY = LineGraphSeries()
        mSeriesZ = LineGraphSeries()

        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(0.0)
        graphView.viewport.setMaxX(200.0)

        mSeriesX.color = Color.BLUE
        mSeriesY.color = Color.RED
        mSeriesZ.color = Color.GREEN

        mSeriesX.title = "X"
        mSeriesY.title = "Y"
        mSeriesZ.title = "Z"

        graphView.addSeries(mSeriesX)
        graphView.addSeries(mSeriesY)
        graphView.addSeries(mSeriesZ)

        graphView.legendRenderer.isVisible = true
        graphView.legendRenderer.align = LegendRenderer.LegendAlign.TOP
    }

    override fun onStart() {
        super.onStart()
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS), 2522)
    }
    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mSensorAccelerate, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    var x = 0
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && mReadSensorsTrigger) {
            val accelerateX = event.values[0]
            val accelerateY = event.values[1]
            val accelerateZ = event.values[2]

            mAccelerateY.text = accelerateY.toString()
            mAccelerateX.text = accelerateX.toString()
            mAccelerateZ.text = accelerateZ.toString()

            when {
                Math.abs(accelerateX) > accelerateXMaxValue -> accelerateXMaxValue = Math.abs(accelerateX)
                Math.abs(accelerateY) > accelerateYMaxValue -> accelerateYMaxValue = Math.abs(accelerateY)
                Math.abs(accelerateZ) > accelerateZMaxValue -> accelerateZMaxValue = Math.abs(accelerateZ)
            }

            Log.i("accelerate x axis", event.values[0].toString())
            Log.i("accelerate y axis", event.values[1].toString())
            Log.i("accelerate z axis", event.values[2].toString())

            mSeriesX.appendData(DataPoint(x++.toDouble(), event.values[0].toDouble()), true, 200)
            mSeriesY.appendData(DataPoint(x++.toDouble(), event.values[1].toDouble()), true, 200)
            mSeriesZ.appendData(DataPoint(x++.toDouble(), event.values[2].toDouble()), true, 200)
        }

    }


    fun onToggleClicked(view: View) {
        mReadSensorsTrigger = !mReadSensorsTrigger
        val sensorServiceIntent = Intent(baseContext, AccelerationService::class.java)
        mToggleButton.text = if (mReadSensorsTrigger) "Стоп" else "Старт"
        if (!mReadSensorsTrigger) {
            stopService(sensorServiceIntent)
        } else {
            startService(sensorServiceIntent)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_settings -> openSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSettings(): Boolean {
        val openSettingIntent = Intent(this, SettingsActivity::class.java)
        startActivity(openSettingIntent)
        return true
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("myChannel", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}
