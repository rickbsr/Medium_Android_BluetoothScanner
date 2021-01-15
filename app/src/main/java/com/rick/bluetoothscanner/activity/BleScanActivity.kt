package com.rick.bluetoothscanner.activity

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SimpleAdapter
import com.rick.bluetoothscanner.R
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.HashMap

private const val REQUEST_ENABLE_BT = 8

class BleScanActivity : AppCompatActivity() {

    companion object {
        private val TAG = BleScanActivity::class.java.simpleName
    }

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    private var mIsScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        initView()
        initBlueAdapter()
    }

    private fun initBlueAdapter() {
        Log.d(TAG, "initBlueAdapter: ")
        mBluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        checkedBluetoothEnable()
    }

    private fun checkedBluetoothEnable() {
        Log.d(TAG, "checkedBluetoothEnable: ")
        if (!mBluetoothAdapter.isEnabled) {
            Log.d(TAG, "Bluetooth isn't Enable")

            // By enable()
//            mBluetoothAdapter.enable()

            // By intent
            startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        }
    }

    private fun initView() {
        btn_scan.setOnClickListener {
            scanBleDevices()
        }

        lv_devices.setOnItemClickListener { parent, _, position, _ ->
            if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
            val o: HashMap<*, *> = parent?.getItemAtPosition(position) as HashMap<*, *>
            val deviceName: String? = o["name"] as String?
            val deviceAddress: String? = o["address"] as String?

            AlertDialog.Builder(this)
                .setTitle("Bluetooth Device")
                .setMessage("Name: $deviceName, Address: $deviceAddress")
                .setNeutralButton("OK", null)
                .show()
        }
    }

    private fun scanBleDevices() {
        Log.d(TAG, "scanBleDevices: ")
        val bluetoothLeScanner by lazy { mBluetoothAdapter.bluetoothLeScanner }
        if (!mIsScanning) {
            bluetoothLeScanner.startScan(mScanCallback)
            runOnUiThread {
                btn_scan.text = "STOP"
                progressBar.visibility = View.VISIBLE
                mIsScanning = true
            }
        } else {
            bluetoothLeScanner.stopScan(mScanCallback)
            runOnUiThread {
                btn_scan.text = "START"
                progressBar.visibility = View.GONE
                mIsScanning = false
            }
        }
    }

    private val mScanCallback = object : ScanCallback() {

        private val deviceSet: MutableSet<BluetoothDevice> = mutableSetOf()

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "onScanFailed: ")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult: ${result?.scanRecord.toString()}")

            val device: BluetoothDevice = result!!.device
            if (deviceSet.add(device)) setListView()
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "onBatchScanResults: ")
        }

        private fun setListView() {
            val devices = mutableListOf<Map<String, String>>()
            for (d in deviceSet) {
                val map =
                    mapOf<String, String>("name" to d.name, "address" to d.address)
                devices.add(map)
            }
            val adapter = SimpleAdapter(
                this@BleScanActivity,
                devices,
                android.R.layout.simple_list_item_2,
                listOf("name", "address").toTypedArray(),
                listOf(android.R.id.text1, android.R.id.text2).toIntArray()
            )
            runOnUiThread {
                lv_devices.adapter = adapter
            }
        }
    }
}