package com.example.imageslist

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
    private val imageRows: List<ImageRow>
) : RecyclerView.Adapter<ImageRowAdapter.ImageRowViewHolder>() {
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

                    DownloadImagesTask(
                        url,
                        holder.adapterPosition,
                        WeakReference(this@ImageRowAdapter)
                    ).execute()
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

    class DownloadImagesTask(val url: String, val position: Int, val activity : WeakReference<ImageRowAdapter>) :
        AsyncTask<Unit, Unit, Bitmap>() {
        override fun doInBackground(vararg params: Unit?): Bitmap {
            URL(url).openStream().use {
                val bitmap = BitmapFactory.decodeStream(it)

                val aspectRatio = bitmap.width.toDouble() / bitmap.height.toDouble()
                val (newHeight, newWidth) = with(bitmap) {
                    if (height > width) {
                        Pair(
                            height.coerceAtMost(MyIntentService.MAX_BITMAP_SIZE), (height.coerceAtMost(
                                MyIntentService.MAX_BITMAP_SIZE
                            ) * aspectRatio).toInt()
                        )
                    } else {
                        Pair(
                            (width.coerceAtMost(MyIntentService.MAX_BITMAP_SIZE) / aspectRatio).toInt(),
                            width.coerceAtMost(MyIntentService.MAX_BITMAP_SIZE)
                        )
                    }
                }
                return Bitmap.createScaledBitmap(
                    bitmap,
                    newWidth, newHeight, true
                )
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            map[url] = result
            activity.get()?.addBitmap(url, position)
        }

        override fun onCancelled() {
            super.onCancelled()
        }
    }

    fun addBitmap(url: String, position : Int) {
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