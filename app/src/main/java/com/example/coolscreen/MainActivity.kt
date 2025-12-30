package com.example.coolscreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var toggleButton: Button
    
    // Используем SharedPreferences, чтобы помнить состояние после перезапуска
    private val prefs by lazy { getSharedPreferences("CoolScreenPrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleButton = findViewById(R.id.btnToggle)
        
        // Восстанавливаем состояние кнопки при запуске
        val isRunning = prefs.getBoolean("is_running", false)
        updateButtonState(isRunning)

        toggleButton.setOnClickListener {
            // Проверяем разрешение перед каждым переключением
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return@setOnClickListener
            }

            val currentState = prefs.getBoolean("is_running", false)
            if (currentState) {
                stopOverlayService()
            } else {
                startOverlayService()
            }
        }

        // Если открыли первый раз и разрешения нет — просим сразу
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun startOverlayService() {
        val intent = Intent(this, FilterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        // Сохраняем состояние "Включено"
        prefs.edit().putBoolean("is_running", true).apply()
        updateButtonState(true)
    }

    private fun stopOverlayService() {
        val intent = Intent(this, FilterService::class.java)
        stopService(intent)
        
        // Сохраняем состояние "Выключено"
        prefs.edit().putBoolean("is_running", false).apply()
        updateButtonState(false)
    }

    private fun updateButtonState(isRunning: Boolean) {
        toggleButton.text = if (isRunning) "Выключить" else "Включить"
    }
}
