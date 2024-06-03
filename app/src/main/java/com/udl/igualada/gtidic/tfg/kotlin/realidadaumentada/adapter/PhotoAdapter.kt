package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view.PhotoDetailActivity

class PhotoAdapter(private val context: Context, private val photoList: List<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewFilename: TextView = itemView.findViewById(R.id.textViewFilename)
        val textViewComment: TextView = itemView.findViewById(R.id.textViewComment)
        val textViewModelName: TextView = itemView.findViewById(R.id.textViewModelName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.photo_list_item, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photoList[position]
        val url = photo.url
        val filename = photo.filename
        val comment = photo.comment
        val modelName = photo.modelName

        if (url != null) {
            Glide.with(context).load(url).into(holder.imageView)
        }

        holder.textViewFilename.text = filename ?: "Unknown"
        holder.textViewComment.text = comment ?: "No comment"
        holder.textViewModelName.text = modelName ?: "Unknown"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PhotoDetailActivity::class.java)
            intent.putExtra("filename", filename)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }
}
