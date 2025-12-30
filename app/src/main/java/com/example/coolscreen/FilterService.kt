package com.example.coolscreen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
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
        
        // 1. Запуск Foreground Service (обязательно для Android 14/15)
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CoolScreen")
            .setContentText("Фильтр активен")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Чтобы не шумело
            .build()
            
        // ID уведомления должен быть > 0
        startForeground(101, notification)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 2. Создаем View фильтра
        overlayView = View(this)
        
        // ПЛАН Б: Простая заливка цветом
        // Формат ARGB: 
        // Alpha = 0x0A (около 4% прозрачности) - можно менять от 05 до 15
        // Red=D0, Green=E0, Blue=FF (Холодный голубой)
        // 
        // Если будет слишком слабо — поменяйте 0x0A на 0x10 или 0x15
        // Если будет слишком видно на черном — поменяйте на 0x05
        overlayView?.setBackgroundColor(0x0AD0E0FF.toInt())

        // 3. Параметры окна (Оверлей)
        val params = WindowManager.LayoutParams().apply {
            // Тип окна: поверх всех приложений
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            // Флаги:
            // FLAG_NOT_TOUCHABLE - пропускать нажатия сквозь фильтр (Самое важное!)
            // FLAG_NOT_FOCUSABLE - не перехватывать клавиатуру
            // FLAG_LAYOUT_IN_SCREEN - рисовать даже под статус-баром и вырезом камеры
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS // На весь-весь экран

            // Формат: Прозрачный фон (чтобы работала альфа)
            format = PixelFormat.TRANSLUCENT
            
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        // Добавляем View на экран
        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CoolScreen Service",
                NotificationManager.IMPORTANCE_MIN // Минимальная важность (без звука)
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "CoolScreenChannel"
    }
}
