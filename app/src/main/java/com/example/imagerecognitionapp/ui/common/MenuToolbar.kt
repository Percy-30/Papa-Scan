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


class MenuToolbar(
    private val context: Context,
    private val onHistoryClick: () -> Unit = {},
    private val onAboutClick: () -> Unit = {},
    private val onExitClick: () -> Unit = {}
) {
    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_history -> {
                onHistoryClick()
                true
            }
            R.id.menu_about -> {
                onAboutClick()
                true
            }
            R.id.menu_exit -> {
                onExitClick()
                true
            }
            else -> false
        }
    }
}