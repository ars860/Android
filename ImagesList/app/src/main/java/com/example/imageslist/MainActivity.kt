package com.example.imageslist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.URL

val POSITION_TAG: String = "position"

class MainActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_TAG = "android_eto_kakoi_to_kostyl"
    }

    private var imageRows: MutableList<ImageRow> = mutableListOf()
    private var bitmapsSavingFragment: BitmapsSavingFragment? = null
    private val receiver: ResponseReceiver = ResponseReceiver()

    fun initFragment() {
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
            adapter = ImageRowAdapter(imageRows)
        }

        val filter = IntentFilter(ACTION_LOAD_IMAGE_OUT)
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
            val position = intent.getIntExtra(POSITION_TAG, -1)
            val url = intent.getStringExtra(URL_TAG)

            if (url != null && position != -1) {
                addBitmap(url, position)
            }
        }

        fun addBitmap(url: String, position : Int) {
            with(imageRows[position]) {
                if (!loaded) {
                    this.bitmap = map[url]
                    loading = false
                    loaded = true

                    recyclerView.adapter?.notifyItemChanged(position)
                }
            }
        }
    }
}