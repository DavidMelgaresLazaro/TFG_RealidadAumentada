package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view.PhotoDetailActivity

class PhotoAdapter(
    private val context: Context,
    private val photos: List<Photo>,
    private val photoKeys: List<String>
) : BaseAdapter() {

    override fun getCount(): Int {
        return photos.size
    }

    override fun getItem(position: Int): Any {
        return photos[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.photo_list_item, parent, false)

        val photo = photos[position]
        val photoKey = photoKeys[position]

        val imageView = view.findViewById<ImageView>(R.id.photoImageView)
        val textViewComment = view.findViewById<TextView>(R.id.textViewComment)
        val textViewModelName = view.findViewById<TextView>(R.id.textViewModelName)

        Glide.with(context).load(photo.url).into(imageView)
        textViewComment.text = photo.comment
        textViewModelName.text = photo.modelName

        view.setOnClickListener {
            val intent = Intent(context, PhotoDetailActivity::class.java).apply {
                putExtra("photoKey", photoKey) // Asegúrate de que se pase la clave de la foto
            }
            context.startActivity(intent)
        }

        view.setOnLongClickListener {
            showDeleteDialog(photoKey, position)
            true
        }

        return view
    }

    private fun showDeleteDialog(photoKey: String, position: Int) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete this photo?")
            .setPositiveButton("Yes") { _, _ ->
                deletePhoto(photoKey, position)
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    private fun deletePhoto(photoKey: String, position: Int) {
        val database = FirebaseDatabase.getInstance().reference.child("photos").child(photoKey)
        database.removeValue().addOnSuccessListener {
            (photos as MutableList).removeAt(position)
            (photoKeys as MutableList).removeAt(position)
            notifyDataSetChanged()
        }.addOnFailureListener {
            // Handle failure
        }
    }
}
