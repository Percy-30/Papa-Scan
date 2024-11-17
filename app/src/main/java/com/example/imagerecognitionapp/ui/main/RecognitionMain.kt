package com.example.imagerecognitionapp.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.databinding.ActivityMainBinding
import com.example.imagerecognitionapp.databinding.FragmentRecognitionMainBinding

class RecognitionMain : Fragment() {

    private lateinit var binding: FragmentRecognitionMainBinding
    private val model: MainViewModel by activityViewModels()


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
        binding = FragmentRecognitionMainBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_recognition_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Use the View object to perform additional initialization
        /*model.operacionExitosa.observe(viewLifecycleOwner, Observer {
            if(it > 0){
                Toast.makeText(requireContext(),"Exito al Guardar" , Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.recognitionMain)
            }
            else if(it == 0){
                Toast.makeText(requireContext(),"Ocurrio un Error" , Toast.LENGTH_SHORT).show()
            }
        })*/
        binding.btnDetectPatofoli.setOnClickListener {
            findNavController().navigate(R.id.recognitionFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    // Ejemplo de método para mostrar un diálogo de "Acerca de"
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.btnAbout))
            .setMessage("Esta es una app de reconocimiento de imágenes.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecognitionMain().apply {
                arguments = Bundle().apply {
                }
            }
    }
}