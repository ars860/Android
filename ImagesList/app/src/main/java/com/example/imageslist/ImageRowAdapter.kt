package com.example.imageslist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.image_row.view.*
import java.lang.ref.WeakReference
import java.net.URL


data class ImageRow(
    val name: String,
    val url: String,
    var loaded: Boolean = false,
    var loading: Boolean = false,
    var bitmap: Bitmap? = null
)

class ImageRowAdapter(
    private val imageRows: List<ImageRow>,
    private val applicationContext: WeakReference<Context>
) : RecyclerView.Adapter<ImageRowAdapter.ImageRowViewHolder>() {
    private var currentTask: MainActivity.DownloadImagesTask? = null

    class ImageRowViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
        fun bind(imageRow: ImageRow) {
            with(root) {
                name.text = imageRow.name
                name.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    BitmapDrawable(imageRow.bitmap),
                    null,
                    null
                )
                if (imageRow.loading) {
                    progress.visibility = View.VISIBLE
                } else {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageRowViewHolder {
        val holder = ImageRowViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.image_row, parent, false)
        )
        holder.root.setOnClickListener {
            if (imageRows[holder.adapterPosition].loading) {
                return@setOnClickListener
            }

            with(imageRows[holder.adapterPosition]) {
                if (!loaded) {
                    loading = true
                    notifyItemChanged(holder.adapterPosition)

                    if (!isMultiLoad){
                        currentTask?.cancel(false)
                    }

                    currentTask = MainActivity.DownloadImagesTask(
                        url,
                        holder.adapterPosition,
                        applicationContext
                    ).apply { execute() }
//                    MyIntentService.startLoadImage(parent.context, url, holder.adapterPosition)
                } else {
                    loaded = false
                    bitmap?.recycle()
                    bitmap = null
                    notifyItemChanged(holder.adapterPosition)
                }

            }
        }
        return holder
    }

    override fun getItemCount() = imageRows.size

    override fun onBindViewHolder(holder: ImageRowViewHolder, position: Int) =
        holder.bind(imageRows[position])


    fun addBitmap(url: String, position: Int) {
        with(imageRows[position]) {
            if (!loaded) {
                this.bitmap = map[url]
                loading = false
                loaded = true

                notifyItemChanged(position)
            }
        }
    }
}