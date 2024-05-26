package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo

class PhotoDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        val imageView: ImageView = findViewById(R.id.imageViewDetailPhoto)
        val textViewModelName: TextView = findViewById(R.id.textViewDetailModelName)
        val textViewComment: TextView = findViewById(R.id.textViewDetailComment)
        val textViewTime: TextView = findViewById(R.id.textViewDetailTime)
        val textViewSize: TextView = findViewById(R.id.textViewDetailSize)
        val textViewPosition: TextView = findViewById(R.id.textViewDetailPosition)
        val textViewDistance: TextView = findViewById(R.id.textViewDetailDistance)

        val photo = intent.getParcelableExtra<Photo>("photo")

        photo?.let {
            Glide.with(this).load(it.url).into(imageView)
            textViewModelName.text = it.modelName
            textViewComment.text = it.comment
            textViewTime.text = it.time
            textViewSize.text = "Size: ${it.size}"
            textViewPosition.text = "Position: ${it.position}"
            textViewDistance.text = "Distance: ${it.distance}"
        }
    }
}
