package ru.bstu.vt41.davydov.vkr.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast
import net.danlew.android.joda.JodaTimeAndroid
import ru.bstu.vt41.davydov.vkr.R
import java.util.*


class AccelerationService : Service(), SensorEventListener {

    private lateinit var mSensorAccelerate: Sensor
    private lateinit var mSensorManager: SensorManager

    private var mAccelerateValuesXAxis: ArrayDeque<Float> = ArrayDeque(3)
    private var mAccelerateValuesYAxis: ArrayDeque<Float> = ArrayDeque(3)
    private var mAccelerateValuesZAxis: ArrayDeque<Float> = ArrayDeque(3)

    private val mLimitXAxis = 12 * 9.82
    private val mLimitYAxis = 9 * 9.82
    private val mLimitZAxis = 10 * 9.82
    private val notificationId = 1488

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            moveDeque(mAccelerateValuesXAxis, event.values[0], 3)
            moveDeque(mAccelerateValuesYAxis, event.values[1], 3)
            moveDeque(mAccelerateValuesZAxis, event.values[2], 3)


            val xAvg = mAccelerateValuesXAxis.average()
            val yAvg = mAccelerateValuesYAxis.average()
            val zAvg = mAccelerateValuesZAxis.average()

            //Расчет индекса ущерба ASI
            val damage = Math.sqrt(
                    Math.pow(xAvg / mLimitXAxis, 2.0)
                            + Math.pow(yAvg / mLimitYAxis, 2.0)
                            + Math.pow(zAvg / mLimitZAxis, 2.0)
            )

            if(damage >= 0.3){
                val builder = NotificationCompat.Builder(this, "myChannel")
                        .setSmallIcon(R.drawable.ic_mood_bad_black_24dp)
                        .setContentTitle("Вы в порядке?")
                        .setContentText("Система зафиксировала столкновение")
                        .setPriority(NotificationCompat.PRIORITY_MAX)


                NotificationManagerCompat.from(this).notify(notificationId, builder.build())

                val notifyServiceIntent = Intent(baseContext, NotifyService::class.java)
                startService(notifyServiceIntent)
                stopSelf()
            }

        }
    }

    private fun moveDeque(deque: ArrayDeque<Float>, value: Float, maxDequeSize: Int) {
        deque.push(value)
        if (deque.size >= maxDequeSize) deque.pollLast()
    }

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorAccelerate =
                if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                } else {
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                }
        mSensorManager.registerListener(this, mSensorAccelerate, SensorManager.SENSOR_DELAY_GAME) //50 Гц

        Toast.makeText(baseContext, "Сервис запущен", Toast.LENGTH_SHORT).show()

        return START_STICKY
    }



    override fun onDestroy() {
        mSensorManager.unregisterListener(this)

    }
}
