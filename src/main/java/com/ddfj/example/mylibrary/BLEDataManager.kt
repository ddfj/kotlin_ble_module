package com.ddfj.example.mylibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

object BLEDataManager{
    private val tag = "BLEDataManager"
    private var mOnDeviceBleListener: OnDeviceBleListener? = null

    val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val action = intent.action
            when ( intent.action){
                BLEConstants.ACTION_GATT_CONNECTED -> {
                    Log.e(tag, "ACTION_GATT_CONNECTED")
                }
                BLEConstants.ACTION_GATT_DISCONNECTED -> {
                    Log.e(tag, "ACTION_GATT_DISCONNECTED")
                    mOnDeviceBleListener?.onBleConnectionCompleted(false)
                }
                BLEConstants.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.e(tag, "ACTION_GATT_SERVICES_DISCOVERED")

                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    BLEConnectionManager.findBLEGattService()
                }
                BLEConstants.ACTION_DATA_PARSE -> {
                    val data = intent.getByteArrayExtra(BLEConstants.EXTRA_DATA)
                    val uuId = intent.getStringExtra(BLEConstants.EXTRA_UUID)
                    Log.e(tag, "ACTION_DATA_PARSE $data")
                    mOnDeviceBleListener?.onBleRecData(uuId, data)
                }
            }
        }
    }

    fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BLEConstants.ACTION_DATA_PARSE)
        //intentFilter.addAction(BLEConstants.ACTION_DATA_WRITTEN)

        return intentFilter
    }

}