package com.example.coolscreen

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class FilterService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        
        // Создаём уведомление для Foreground Service
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CoolScreen Active")
            .setContentText("Фильтр экрана включён")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Создаём прозрачное View с цветовым фильтром
        overlayView = object : View(this) {
            private val paint = Paint().apply {
                // Матрица для уменьшения Red и Green каналов
                val colorMatrix = ColorMatrix(floatArrayOf(
                    0.85f, 0f, 0f, 0f, 0f,  // Red канал * 0.85
                    0f, 0.85f, 0f, 0f, 0f,  // Green канал * 0.85
                    0f, 0f, 1.0f, 0f, 0f,   // Blue канал * 1.0 (не трогаем)
                    0f, 0f, 0f, 1f, 0f      // Alpha без изменений
                ))
                colorFilter = ColorMatrixColorFilter(colorMatrix)
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                // Рисуем прозрачный прямоугольник с фильтром
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Filter Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "FilterServiceChannel"
    }
}
