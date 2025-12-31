package com.example.coolscreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. BASE: 0x060000FF
        findViewById<Button>(R.id.btnBase).setOnClickListener { 
            startFilter(0x060000FF) 
        }

        // 2. FAVORITE: 0x0A040899
        findViewById<Button>(R.id.btnFavorite).setOnClickListener { 
            startFilter(0x0A040899.toInt()) 
        }

        // 3. COOL 1: 0x200000FF
        findViewById<Button>(R.id.btnCool1).setOnClickListener { 
            startFilter(0x200000FF.toInt()) 
        }

        // 4. COOL 2: 0x140000FF
        findViewById<Button>(R.id.btnCool2).setOnClickListener { 
            startFilter(0x140000FF.toInt()) 
        }

        // 5. COOL 3: 0x140010FF
        findViewById<Button>(R.id.btnCool3).setOnClickListener { 
            startFilter(0x140010FF.toInt()) 
        }

        // 6. COOL 4: 0x300010FF
        findViewById<Button>(R.id.btnCool4).setOnClickListener { 
            startFilter(0x160000FF.toInt()) 
        }


        // ВЫКЛЮЧИТЬ
        findViewById<Button>(R.id.btnOff).setOnClickListener { 
            stopFilter() 
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun startFilter(color: Int) {
        if (!Settings.canDrawOverlays(this)) {
            checkPermissions()
            return
        }
        val intent = Intent(this, FilterService::class.java)
        intent.putExtra("COLOR_VALUE", color)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFilter() {
        stopService(Intent(this, FilterService::class.java))
    }
}
