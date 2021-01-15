package com.rick.bluetoothscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.rick.bluetoothscanner.activity.BleScanActivity
import com.rick.bluetoothscanner.activity.BtScanActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_PERMISSION_LOCATION = 51

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_btScan.setOnClickListener {
            if (checkedBluetoothPermission())
                startActivity(Intent(this, BtScanActivity::class.java))
        }

        btn_bleScan.setOnClickListener {
            if (checkedBluetoothPermission())
                startActivity(Intent(this, BleScanActivity::class.java))
        }
    }

    private fun checkedBluetoothPermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION
            )
            false
        } else true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION &&
            permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: true")
        }
    }
}