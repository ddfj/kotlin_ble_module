package com.ddfj.example.mylibrary

import android.bluetooth.BluetoothGattCharacteristic
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

object BLEConnectionManager {
    private const val tag = "BLEConnectionManager"
    private var gattServiceUUID = ""
    private var setCharacteristic = ArrayList<String>()
    private var mOnDeviceBleListener: OnDeviceBleListener? = null
    private var mBLEService: BLEService? = null
    private var isBind = false
    private var mDataBLEForControl: BluetoothGattCharacteristic? = null
    private var mDataBLEForMeas: BluetoothGattCharacteristic? = null
    private var mDataBLEForLog: BluetoothGattCharacteristic? = null

    fun setUUID(inStr: String, inCharacteristic:ArrayList<String>){
        gattServiceUUID = inStr

        for (item in inCharacteristic){
            setCharacteristic.add(item)
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Log.e(tag, "onServiceConnected")

            mBLEService = (service as BLEService.LocalBinder).getService()

            if (!mBLEService?.initialize()!!) {
                Log.e(tag, "Unable to initialize")
            }
            else{
                mOnDeviceBleListener?.onBleServiceOpen(true)
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.e(tag, "onServiceDisconnected")
            mBLEService = null
        }
    }

    fun setListener(onDeviceScanListener: OnDeviceBleListener) {
        mOnDeviceBleListener = onDeviceScanListener
    }
    /**
     * Initialize Bluetooth service.
     */
    fun initBLEService(context: Context) {
        try {
            if (mBLEService == null) {
                val gattServiceIntent = Intent(context, BLEService::class.java)
                /*if (context != null) {
                    isBind = context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
                }*/
                context.let{
                    isBind = context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, e.message)
        }
    }

    /**
     * Unbind BLE Service
     */
    fun unBindBLEService(context: Context) {
        /*if (mServiceConnection != null && isBind) {
            context.unbindService(mServiceConnection)
        }*/
        if (isBind){
            mServiceConnection.let{
                context.unbindService(mServiceConnection)
            }
        }
        mBLEService = null
    }

    /**
     * Connect to a BLE Device
     */
    fun connect(deviceAddress: String): Boolean {
        var result = false

        if (mBLEService != null) {
            result = mBLEService!!.connect(deviceAddress)
        }
        return result
    }

    /**
     * Disconnect
     */
    fun disconnect() {
        /*if (null != mBLEService) {
            mBLEService!!.disconnect()
            mBLEService = null
        }*/
        mBLEService?.let{
            mBLEService!!.disconnect()
            mBLEService = null
        }
    }

    fun send(cmd: Int) {
        /*if (mDataBLEForControl != null) {
            mDataBLEForControl!!.value = byteArrayOf(cmd.toByte())
            writeBLECharacteristic(mDataBLEForControl)
        }*/
        Log.e(tag, "send : $cmd")

        mDataBLEForControl.let{
            mDataBLEForControl!!.value = byteArrayOf(cmd.toByte())
            writeBLECharacteristic(mDataBLEForControl)
        }
    }

    fun send(inData: ByteArray) {
        /*if (mDataBLEForControl != null) {
            mDataBLEForControl!!.value = byteArrayOf(cmd.toByte())
            writeBLECharacteristic(mDataBLEForControl)
        }*/
        Log.e(tag, "send : ByteArray")

        mDataBLEForControl.let{
            mDataBLEForControl!!.value = inData
            writeBLECharacteristic(mDataBLEForControl)
        }
    }

    fun send(cmd: Int, size: Int, data: ByteArray) {
        //if (mDataBLEForControl != null) {
        mDataBLEForControl.let{
            val sData = ByteArray(size + 2)
            var add = 0
            sData[add++] = cmd.toByte()
            //sData[add++] = size.toByte()
            sData[add++] = 0x11
            for (i in data.indices){
                sData[add++] = data[i]
            }
            mDataBLEForControl!!.value = sData
            writeBLECharacteristic(mDataBLEForControl)
        }
    }

    /**
     * Write BLE Characteristic.
     */
    private fun writeBLECharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (null != characteristic) {
            //if (mBLEService != null) {
            mBLEService.let{
                mBLEService?.writeCharacteristic(characteristic)
            }
        }
    }


    /**
     * findBLEGattService
     */
    fun findBLEGattService() {
        if (mBLEService == null) {
            return
        }

        if (mBLEService!!.getSupportedGattServices() == null) {
            return
        }

        var uuid: String
        mDataBLEForControl = null
        mDataBLEForMeas = null
        mDataBLEForLog = null

        val serviceList = mBLEService!!.getSupportedGattServices()

        if (serviceList != null) {
            var flagResult = false

            for (gattService in serviceList) {
                //if (gattService.getUuid().toString().equals(mContext.getString(R.string.char_uuid_emergency), ignoreCase = true)) {
                if (gattService.uuid.toString().equals(gattServiceUUID, ignoreCase = true)) {
                    val gattCharacteristics = gattService.characteristics

                    for (gattCharacteristic in gattCharacteristics) {
                        uuid = if (gattCharacteristic.uuid != null) gattCharacteristic.uuid.toString() else ""
                        Log.e(tag, "findBLEGattService read UUID= $uuid")
                        for (setId in setCharacteristic.withIndex()){
                            if (setId.value == uuid){
                                var newChar = gattCharacteristic
                                newChar = setProperties(newChar)

                                when(setId.index){
                                    0 -> mDataBLEForControl = newChar
                                    1 -> mDataBLEForMeas = newChar
                                    2 -> mDataBLEForLog = newChar
                                }
                                break
                            }
                        }
                        /*when(uuid){
                            "00001524-0000-1000-8000-00805f9b34fb" -> {
                                var newChar = gattCharacteristic
                                newChar = setProperties(0, newChar)
                                mDataBLEForControl = newChar
                                //mDataBLEForControl = gattService.getCharacteristic(gattCharacteristic.uuid)
                            }

                            //mContext.resources.getString(R.string.char_uuid_mes) -> {
                            "00001525-0000-1000-8000-00805f9b34fb" -> {
                                var newChar = gattCharacteristic
                                newChar = setProperties(1, newChar)
                                mDataBLEForMeas = newChar
                                //mDataBLEForMeas = gattService.getCharacteristic(gattCharacteristic.uuid)
                            }

                            // mContext.resources.getString(R.string.char_uuid_log) -> {
                            "00001526-0000-1000-8000-00805f9b34fb" -> {
                                var newChar = gattCharacteristic
                                newChar = setProperties(1, newChar)
                                mDataBLEForLog = newChar
                                //mDataBLEForLog = gattService.getCharacteristic(gattCharacteristic.uuid)
                            }
                        }*/

                        /*if (uuid.equals(mContext.resources.getString(R.string.char_uuid_control), ignoreCase = true)) {
                            var newChar = gattCharacteristic
                            newChar = setProperties(newChar)
                            mDataBLEForControl = newChar
                        }
                        else if (uuid.equals(mContext.resources.getString(R.string.char_uuid_mes), ignoreCase = true)) {
                            var newChar = gattCharacteristic
                            newChar = setProperties(newChar)
                            mDataBLEForMeas = newChar
                        }
                        else if (uuid.equals(mContext.resources.getString(R.string.char_uuid_log), ignoreCase = true)) {
                            var newChar = gattCharacteristic
                            newChar = setProperties(newChar)
                            mDataBLEForLog = newChar
                        }

                        if (mDataBLEForControl != null && mDataBLEForMeas != null && mDataBLEForLog != null){
                            flagResult = true
                            break
                        }*/
                    }
                }
            }

            if (mDataBLEForControl != null && mDataBLEForMeas != null && mDataBLEForLog != null){
                flagResult = true
            }

            Log.e(tag, "findBLEGattService flagResult= $flagResult")
            mOnDeviceBleListener?.onBleConnectionCompleted(flagResult)
        }

    }

    private fun setProperties(gattCharacteristic: BluetoothGattCharacteristic):
            BluetoothGattCharacteristic {
        val characteristicProperties = gattCharacteristic.properties

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            mBLEService?.setCharacteristicNotification(gattCharacteristic, true)
        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
            mBLEService?.setCharacteristicIndication(gattCharacteristic, true)
        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
            //gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
            gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            gattCharacteristic.writeType = BluetoothGattCharacteristic.PROPERTY_READ
        }

        return gattCharacteristic
    }
}