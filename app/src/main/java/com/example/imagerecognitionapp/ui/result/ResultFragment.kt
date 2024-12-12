package com.example.imagerecognitionapp.ui.result

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.databinding.FragmentResultBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar

class ResultFragment : Fragment() {

    private var _binding:FragmentResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuHandler: MenuToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startMenu()
    }

    //********MENU
    private fun startMenu() {
        menuHandler = MenuToolbar(context = requireContext())
        // Configurar la Toolbar
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.appBarMenu.toolbar)
            supportActionBar?.apply {
                title = "Reconocimiento"
                setDisplayHomeAsUpEnabled(true)
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuHandler.onCreateOptionsMenu(menu, inflater)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToRecognitionFragment()
                true
            }
            R.id.menu_Exti -> {
                requireActivity().finish()
                true
            }
            R.id.menu_about ->{
                //showAboutDialog()
                navigateToAlertDialog()
                true
            }
            else -> menuHandler.onOptionsItemSelected(item)
        }
    }

    //*********ALERT DIALOG
    private fun navigateToAlertDialog() {
        try {
            findNavController().navigate(R.id.action_resultFragment_to_fragmentAlertDialog)
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to AlertDialog: ${e.message}")
            Toast.makeText(requireContext(), "Error al mostrar el diálogo", Toast.LENGTH_SHORT).show()
        }
    }

    //Pasar de fragment en fragment
    private fun navigateToRecognitionFragment() {
        findNavController().navigate(R.id.action_resultFragment_to_recognitionFragment)
    }

}