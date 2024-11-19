package com.example.imagerecognitionapp.ui.common

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
//import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.ui.camera.FragmentCamera


class MenuToolbar (private val context: Context,
    private val onAboutClick:(() -> Unit)? = null
) {

    fun setupToolbar(toolbar: Toolbar, title: String) {
        (context as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                this.title = title
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                Toast.makeText(context, "Acerca de seleccionado", Toast.LENGTH_SHORT).show()
                //(context as? AppCompatActivity)
                //onAboutClick?.invoke()
                false
            }
            R.id.menu_Exti  -> {
                (context as?  AppCompatActivity)?.finish() // Salir de la app
                Toast.makeText(context, "EXIT", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }


}