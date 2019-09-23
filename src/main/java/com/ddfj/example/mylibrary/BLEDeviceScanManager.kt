package com.ddfj.example.mylibrary

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import java.util.ArrayList

object BLEDeviceScanManager {
    private val registeredDevices = mutableSetOf<BluetoothDevice>()

    private val TAG = "BLEDeviceScanManager"
    private var scanCallback: ScanCallback? = null
    private var mDeviceObject: BleDeviceData? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mHandler: Handler? = null
    private var mOnDeviceScanListener: OnDeviceScanListener? = null
    private var mLeScanCallback: BluetoothAdapter.LeScanCallback? = null
    private var mIsContinuesScan: Boolean = false
    private var scanName = ""

    fun scanNameSet(inStr: String){
        scanName = inStr
    }

    init {
        mHandler = Handler()
        createScanCallBackAboveLollipop()
    }

    /**
     * ScanCallback for Lollipop and above
     * The Callback will trigger the Nearest available BLE devices
     * Search the BLE device in Range and pull the Name and Mac Address from it
     */
    private fun createScanCallBackAboveLollipop() {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)

                if (null != mOnDeviceScanListener && result != null && result.device != null && result.device.address != null) {
                    val data = BleDeviceData()


                    Log.e(TAG, result.device.address + ", " + result.scanRecord?.deviceName)
                    //data.mDeviceName = if (result.device.name != null) result.device.name else "Unknown"
                    // data.mDeviceAddress = (result.device.address)

                    data.mDeviceName = if (result.scanRecord?.deviceName != null) result.scanRecord.deviceName else "Unknown"
                    data.mDeviceAddress = (result.device.address)


                    /**
                     * Save the Valid Device info into a list
                     * The List will display to the UI as a popup
                     * User has an option to select one BLE from the popup
                     * After selecting one BLE, the connection will establish and
                     * communication channel will create if its valid device.
                     */
                    if (data.mDeviceName.contains(scanName)) {
                        data.mDeviceRssi = result.rssi
                        mDeviceObject = data

                        mOnDeviceScanListener?.onScanCompleted(mDeviceObject!!)
                    }
                    /*if (data.mDeviceName.contains("FR:R20:SN0338") || data.mDeviceName.
                                    contains("invisa")) {
                        mDeviceObject = data
                        stopScan(mDeviceObject)
                    }*/
                }
            }
        }
    }

    /**
     * Initialize BluetoothAdapter
     * Check the device has the hardware feature BLE
     * Then enable the hardware,
     */
    fun init(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        return mBluetoothAdapter != null && context.packageManager.
            hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * Check bluetooth is enabled or not.
     */
    fun isEnabled(): Boolean {
        return mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled
    }

    /**
     * setListener
     */
    fun setListener(onDeviceScanListener: OnDeviceScanListener) {
        mOnDeviceScanListener = onDeviceScanListener
    }

    /**
     * Scan The BLE Device
     * Check the available BLE devices in the Surrounding
     * If the device is Already scanning then stop Scanning
     * Else start Scanning and check 10 seconds
     * Send the available devices as a callback to the system
     * Finish Scanning after 10 Seconds
     */
    fun scanBLEDevice() {
        try {
            if (mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled) {
                scan()
            }
            /**
             * Stop Scanning after a Period of Time
             * Set a 10 Sec delay time and Stop Scanning
             * collect all the available devices in the 10 Second
             */
            /* if (!isContinuesScan) {
                 mHandler?.postDelayed({
                     // Set a delay time to Scanning
                     stopScan(mDeviceObject)
                 }, BLEConstants.SCAN_PERIOD) // Delay Period
             }*/
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    private fun scan() {
        mBluetoothAdapter?.bluetoothLeScanner?.startScan(null, scanSettings(), scanCallback) // Start BLE device Scanning in a separate thread
    }

    private fun scanFilters(): List<ScanFilter> {
        val emergencyUDID = "00001523-1212-efde-1523-785feabcd123"// Your UUID
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(emergencyUDID)).build()
        val list = ArrayList<ScanFilter>(1)
        list.add(filter)
        return list
    }

    private fun scanSettings(): ScanSettings {
        //return ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build
        return ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()
    }

    fun stopScan() {
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled && scanCallback != null) {
                if (mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled) { // check if its Already available
                    mBluetoothAdapter!!.bluetoothLeScanner.stopScan(scanCallback)
                }
            }
        }
    }
}