package com.rick.bluetoothscanner.activity

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.rick.bluetoothscanner.MainActivity
import com.rick.bluetoothscanner.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.HashMap

private const val REQUEST_ENABLE_BT = 8

class BtScanActivity : AppCompatActivity() {

    companion object {
        private val TAG = BtScanActivity::class.java.simpleName
    }

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        initView()
        initBlueAdapter()
        registerReceivers()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
    }

    private fun registerReceivers() {
        Log.d(TAG, "registerReceivers: ")
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mBroadcastReceiver, filter)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {

        private val deviceSet: MutableSet<BluetoothDevice> = mutableSetOf()

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(TAG, "onReceive: ACTION_FOUND: $device.name, $device.address ")
                    if (deviceSet.add(device)) setListView()
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val perState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    val nowState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    Log.d(TAG, "ACTION_STATE_CHANGED: $perState")

                    when (nowState) {
                        BluetoothAdapter.STATE_TURNING_ON ->
                            Log.d(TAG, "ACTION_STATE_CHANGED: STATE_TURNING_ON")
                        BluetoothAdapter.STATE_ON ->
                            Log.d(TAG, "ACTION_STATE_CHANGED: STATE_ON")
                        BluetoothAdapter.STATE_TURNING_OFF ->
                            Log.d(TAG, "ACTION_STATE_CHANGED: STATE_TURNING_OFF")
                        BluetoothAdapter.STATE_OFF ->
                            Log.d(TAG, "ACTION_STATE_CHANGED: STATE_OFF")
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    runOnUiThread {
                        btn_scan.text = "STOP"
                        progressBar.visibility = View.VISIBLE
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    runOnUiThread {
                        btn_scan.text = "START"
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }

        private fun setListView() {
            val devices = mutableListOf<Map<String, String>>()
            for (d in deviceSet) {
                val map =
                    mapOf<String, String>("name" to d.name, "address" to d.address)
                devices.add(map)
            }
            val adapter = SimpleAdapter(
                this@BtScanActivity,
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
            discoveryBtDevices()
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

    private fun discoveryBtDevices() {
        Log.d(TAG, "discoveryBtDevices: ")
        if (!mBluetoothAdapter.isDiscovering)
            mBluetoothAdapter.startDiscovery()
        else mBluetoothAdapter.cancelDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: $requestCode, $resultCode")
        if (requestCode == REQUEST_ENABLE_BT) {
            when (resultCode) {
                Activity.RESULT_OK -> Log.d(TAG, "REQUEST_ENABLE_BT: OK")
                Activity.RESULT_CANCELED -> Log.d(TAG, "REQUEST_ENABLE_BT: CANCELED")
            }
        }
    }
}