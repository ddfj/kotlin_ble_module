package com.ddfj.example.mylibrary

interface OnDeviceBleListener {
    fun onBleServiceOpen(result: Boolean)
    fun onBleConnectionCompleted(result: Boolean)
    fun onBleRecData(recUUID: String, recData: ByteArray)
}