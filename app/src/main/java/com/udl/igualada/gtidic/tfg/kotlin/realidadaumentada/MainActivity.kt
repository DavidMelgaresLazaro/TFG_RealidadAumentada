package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val arFragmentContainer = findViewById<FragmentContainerView>(R.id.activity_main__container__camera_area)

        arFragmentContainer?.let {
            val arFragment = ArFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main__container__camera_area, arFragment)
                .commit()
        }
    }
}