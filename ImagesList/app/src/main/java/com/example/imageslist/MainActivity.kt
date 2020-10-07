package com.example.imageslist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicReference


const val MAX_BITMAP_SIZE = 3000
const val ACTION_LOAD_IMAGE = "com.example.imageslist.action.LOAD_IMAGE_OUT"
const val ACTION_MARK_LOADED = "dsadasdsad"

const val URL_TAG = "com.example.imageslist.extra.URL"
const val POSITION_TAG: String = "position"

var isMultiLoad = false

val bitmap_atomic: AtomicReference<Bitmap?> = AtomicReference(null)
val map: ConcurrentMap<String, Bitmap> = ConcurrentHashMap()

val lruCache: LruCache<String, Bitmap> = LruCache(5)

class MainActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_TAG = "android_eto_kakoi_to_kostyl"
    }

    private var imageRows: MutableList<ImageRow> = mutableListOf()
    private var bitmapsSavingFragment: BitmapsSavingFragment? = null
    private val receiver: ResponseReceiver = ResponseReceiver()


    private fun initFragment() {
        val fragmentManager: FragmentManager = supportFragmentManager
        bitmapsSavingFragment =
            fragmentManager.findFragmentByTag(FRAGMENT_TAG) as BitmapsSavingFragment?
        if (bitmapsSavingFragment == null) {
            bitmapsSavingFragment = BitmapsSavingFragment()
            fragmentManager.beginTransaction().add(bitmapsSavingFragment!!, FRAGMENT_TAG).commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragment()
        if (bitmapsSavingFragment?.rowsSaved?.isEmpty() != false) {
            updateRows()
        } else {
            bitmapsSavingFragment?.restoreFromFragment(imageRows)
        }

        val viewManager = LinearLayoutManager(this)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = ImageRowAdapter(imageRows, WeakReference(applicationContext))
        }

        val filter = IntentFilter(ACTION_LOAD_IMAGE).apply { addAction(ACTION_MARK_LOADED) }
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(receiver, filter)
    }

    private fun updateRows() {
        FetchImagesTask(this, 1, 30).execute()
    }

    class FetchImagesTask(context: MainActivity, val page: Int, val limit: Int) :
        AsyncTask<Void, Void, List<ImageRow>>() {
        val context: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Void?): List<ImageRow> {
            val pictures = fetchImages(page, limit)
            val picturesAsImageRow: MutableList<ImageRow> = mutableListOf()

            for (pic in pictures) {
                picturesAsImageRow.add(ImageRow(pic.author, pic.download_url))
            }

            return picturesAsImageRow
        }

        override fun onPostExecute(result: List<ImageRow>?) {
            with(context.get()) {
                this?.imageRows?.clear()
                this?.imageRows?.addAll(result ?: listOf())
                this?.recyclerView?.adapter?.notifyDataSetChanged()

                Toast.makeText(
                    this,
                    this?.resources?.getQuantityString(
                        R.plurals.on_get_contacts_success,
                        imageRows.size,
                        imageRows.size
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun fetchImages(page: Int, limit: Int): List<Picture> {
            URL("https://picsum.photos/v2/list?page=${page}&limit=${limit}").openStream()
                .use { urlStream ->
                    InputStreamReader(urlStream).use { inputStream ->
                        BufferedReader(inputStream).use {
                            return Gson().fromJson(
                                it,
                                object : TypeToken<List<Picture>>() {}.type
                            )
                        }
                    }
                }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        bitmapsSavingFragment?.saveToFragment(imageRows)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    inner class ResponseReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_LOAD_IMAGE -> {
                    val position = intent.getIntExtra(POSITION_TAG, -1)
                    val url = intent.getStringExtra(URL_TAG)

                    if (url != null && position != -1) {
                        addBitmap(url, position)
                    }
                }
                ACTION_MARK_LOADED -> {
                    val position = intent.getIntExtra(POSITION_TAG, -1)

                    if (position != -1) {
                        markLoaded(position)
                    }
                }
            }

        }

        private fun addBitmap(url: String, position: Int) {
            with(imageRows[position]) {
                if (!loaded) {
                    bitmap = if (!isMultiLoad){
                        bitmap_atomic.get()
                    } else {
                        map[url]
                    }

                    loading = false
                    loaded = true

                    if (!isMultiLoad) {
                        val showImageIntent = Intent(this@MainActivity, ShowImage::class.java)
                        startActivity(showImageIntent)
                    }

                    recyclerView.adapter?.notifyItemChanged(position)
                }
            }
        }

        private fun markLoaded(position: Int) {
            with(imageRows[position]) {
                loading = false

                recyclerView.adapter?.notifyItemChanged(position)
            }
        }
    }

    class DownloadImagesTask(
        private val url: String,
        private val position: Int,
        private val context: WeakReference<Context>
    ) :
        AsyncTask<Unit, Unit, Unit>() {
        private var stream: BufferedInputStream? = null

        private fun broadcastResult(bitmap: Bitmap) {
            synchronized(lruCache) {
                lruCache.put(url, bitmap)
            }

            val broadcastIntent = Intent()
            broadcastIntent.action = ACTION_LOAD_IMAGE
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
            broadcastIntent.putExtra(URL_TAG, url)
            broadcastIntent.putExtra(POSITION_TAG, position)

            if (!isCancelled) {
                if (!isMultiLoad){
                    bitmap_atomic.set(bitmap)
                } else {
                    map[url] = bitmap
                }
                context.get()?.sendBroadcast(broadcastIntent)
            }
        }

        override fun doInBackground(vararg params: Unit?) {
            try {
                synchronized(lruCache) {
                    if (lruCache[url] != null) {
                        broadcastResult(lruCache[url])
                        return
                    }
                }

                stream = BufferedInputStream(URL(url).openStream())

                stream.use {
                    val bitmap = BitmapFactory.decodeStream(it)

                    val aspectRatio = bitmap.width.toDouble() / bitmap.height.toDouble()
                    val (newHeight, newWidth) = with(bitmap) {
                        if (height > width) {
                            Pair(
                                height.coerceAtMost(MAX_BITMAP_SIZE),
                                (height.coerceAtMost(
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
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        newWidth, newHeight, true
                    )

                    broadcastResult(scaledBitmap)
                }
            } catch (e: Exception) {
                // nothing
            }
        }

        override fun onCancelled() {
            super.onCancelled()

            stream?.close()

            val broadcastIntent = Intent()
            broadcastIntent.action = ACTION_MARK_LOADED
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
            broadcastIntent.putExtra(POSITION_TAG, position)
            context.get()?.sendBroadcast(broadcastIntent)
        }
    }
}