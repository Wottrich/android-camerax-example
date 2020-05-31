package wottrich.github.io.api_camerax_example.view.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import wottrich.github.io.api_camerax_example.R
import java.io.File

/**
 * @author Wottrich
 * @author lucas.wottrich@operacao.rcadigital.com.br
 * @since 30/05/20
 *
 * Copyright © 2020 Api-camerax-example. All rights reserved.
 *
 */

class MediaAdapter(
    context: Context, private var inflater: LayoutInflater = LayoutInflater.from(context)
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    var onMediaListener: MediaListener? = null

    private var mediaList = mutableListOf<File>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder = MediaViewHolder(inflater.inflate(R.layout.row_media, parent, false))

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) = holder.bind(mediaList[holder.adapterPosition])

    fun addItems(newItems: MutableList<File>) {
        mediaList.clear()
        mediaList.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class MediaViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind (file: File) {
            val imageView = view.findViewById<ImageView>(R.id.image_view)
            Glide.with(view).load(file).into(imageView)
            itemView.setOnClickListener {
                onMediaListener?.onClickListener(file)
            }
        }
    }

}