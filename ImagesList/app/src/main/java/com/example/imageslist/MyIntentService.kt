package com.example.imageslist

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class MyIntentService : IntentService("MyIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        val url = intent?.getStringExtra(URL_TAG)
        val position = intent?.getIntExtra(POSITION_TAG, -1)
        if (url != null && position != null && position != -1) {
            handleImageLoad(url, position)
        }
    }

    private fun handleImageLoad(url: String, position: Int) {
        val bitmapScaled = URL(url).openStream().use {
            val bitmap = BitmapFactory.decodeStream(it)
            val aspectRatio = bitmap.width.toDouble() / bitmap.height.toDouble()
            val (newHeight, newWidth) = with(bitmap) {
                if (height > width) {
                    Pair(
                        height.coerceAtMost(MAX_BITMAP_SIZE), (height.coerceAtMost(
                            MAX_BITMAP_SIZE
                        ) * aspectRatio).toInt()
                    )
                } else {
                    Pair(
                        (width.coerceAtMost(MAX_BITMAP_SIZE) / aspectRatio).toInt(),
                        width.coerceAtMost(MAX_BITMAP_SIZE)
                    )
                }
            }
            Bitmap.createScaledBitmap(
                bitmap,
                newWidth, newHeight, true
            )
        }

        map[url] = bitmapScaled;
        val broadcastIntent = Intent()
        broadcastIntent.action = ACTION_LOAD_IMAGE_OUT
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
        broadcastIntent.putExtra(URL_TAG, url)
        broadcastIntent.putExtra(POSITION_TAG, position)
        sendBroadcast(broadcastIntent)
    }

    companion object {
        const val MAX_BITMAP_SIZE = 3000

        @JvmStatic
        fun startLoadImage(context: Context, url: String, position: Int) {
            val intent = Intent(context, MyIntentService::class.java).apply {
                action = ACTION_LOAD_IMAGE_IN
                putExtra(URL_TAG, url)
                putExtra(POSITION_TAG, position)
            }
            context.startService(intent)
        }
    }
}
