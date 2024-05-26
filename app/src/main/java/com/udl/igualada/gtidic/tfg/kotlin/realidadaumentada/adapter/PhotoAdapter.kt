package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo

class PhotoAdapter : ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewPhoto)
        private val textViewComment: TextView = itemView.findViewById(R.id.textViewComment)
        private val textViewModelName: TextView = itemView.findViewById(R.id.textViewModelName)

        fun bind(photo: Photo) {
            textViewComment.text = photo.comment
            textViewModelName.text = photo.modelName
            Glide.with(itemView.context)
                .load(photo.url)
                .into(imageView)
        }
    }
}

class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem == newItem
    }
}
