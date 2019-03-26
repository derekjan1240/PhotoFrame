package com.example.sensor_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class MainActivity : AppCompatActivity() {
    //layout
    internal lateinit var photoSelect: Button
    internal lateinit var photoPath: TextView
    internal lateinit var photoView: ImageView
    internal lateinit var xyz: TextView
    internal  lateinit var gestureState: TextView

    //photos
    internal var photos = arrayOf(R.drawable.rock, R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g)
    internal var photoCurrent =0
    internal var photoCount = photos.size

    //sensor
    lateinit var sensorManager: SensorManager
    var sensor: Sensor? = null

    // gesture state
    var isNextPhotoReady: Boolean = false
//    var isNextPhotoTrigger: Boolean = false
    var isPreviousPhotoReady: Boolean = false
//    var isPreviousPhotoTrigger: Boolean = false
    var isProjectPhotoReady: Boolean = false
//    var isProjectPhotoTrigger: Boolean = false

    private val eventListener = object : SensorEventListener{
        // 當感測器的 精確度改變時，就會觸發
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

        // 當感測器的 測量值改變時，就會觸發
        override fun onSensorChanged(event: SensorEvent?) {
            if(event == null) Log.d("sensor", "事件為空")

            when(event!!.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val xValue = event.values[0]
                    val yValue = event.values[1]
                    val zValue = event.values[2]
                    val xyzValue = "(x:%.2f, y:%.2f, z:%.2f)".format(xValue,yValue,zValue)
                    Log.d("sensor", xyzValue)
                    xyz.text = xyzValue

                    // detect gesture                                                                           next ready trigger  x>1  x<-5          previous ready trigger  x<-1  x>5         project ready trigger  y>8  y<0
                    // gesture next photo
                    if (xValue > 1){
                        isNextPhotoReady = true
//                        gestureState.text = "Next Ready"

                    }else if (xValue<-5 && isNextPhotoReady){
//                        isNextPhotoTrigger = true
                        isNextPhotoReady = false
                        ShowNextPhoto()
                    }

                    // gesture previous photo
                    if (xValue < -1){
                        isPreviousPhotoReady = true
//                        gestureState.text = "Previous ready";

                    }else if (xValue >5 && isPreviousPhotoReady){
//                        isPreviousPhotoTrigger = true
                        isPreviousPhotoReady = false
                        ShowPreviousPhoto()
                    }

                    // gesture project photo
                    if (yValue > 8){
                        isProjectPhotoReady = true
//                        gestureState.text = "Project ready";
                    }else if (yValue<0 && isProjectPhotoReady){
//                        isProjectPhotoTrigger = true
                        isProjectPhotoReady = false
                        ProjectPhotoToServer()
                    }

                }
                else -> {
                    Log.d("sensor", "未知的感測器觸發")
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Lint element from view
        photoSelect = findViewById<Button>(R.id.photoSelect)
        photoPath = findViewById<TextView>(R.id.photoPath)
        photoView = findViewById<ImageView>(R.id.photoView)
        xyz = findViewById<TextView>(R.id.xyzValue)
        gestureState = findViewById(R.id.gestureSate)

        //add Event Listener
        photoSelect.setOnClickListener {
            buttonPress()
        }

        // sensor
        sensorManager = getSystemService( Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // init img name
        photoPath.text = getString(R.string.photo_path, photoCurrent.toString())
    }


    // photo test function
    private fun buttonPress(){
        photoCurrent=(photoCurrent+1)%photoCount
        photoPath.text = getString(R.string.photo_path, photoCurrent.toString())
        photoView.setImageResource(photos[photoCurrent])
    }


    // photo fun
    fun ShowNextPhoto(){
        photoCurrent=(photoCurrent+1)%photoCount
        photoPath.text = getString(R.string.photo_path, photoCurrent.toString())
        photoView.setImageResource(photos[photoCurrent])
    }

    fun ShowPreviousPhoto(){
        photoCurrent=(photoCurrent-1)%photoCount
        if(photoCurrent<0){
            photoCurrent = photoCount-1
        }
        photoPath.text = getString(R.string.photo_path, photoCurrent.toString())
        photoView.setImageResource(photos[photoCurrent])
    }

    fun ProjectPhotoToServer(){

    }


    // sensor fun
    override fun onPause() {
        super.onPause()
        unregisterSensor()
        Log.d("sensor", "onPause")
    }

    override fun onResume() {
        super.onResume()
        registerSensor()
        Log.d("sensor", "onResume")
    }

    fun registerSensor() {
        // 註冊一個 感測器事件監聽器
        sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterSensor() {
        // 解除一個註冊的 感測器事件監聽器
        sensorManager.unregisterListener(eventListener)
    }

}
