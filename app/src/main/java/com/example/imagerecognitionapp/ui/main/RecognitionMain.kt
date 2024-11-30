package com.example.imagerecognitionapp.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.databinding.ActivityMainBinding
import com.example.imagerecognitionapp.databinding.FragmentRecognitionMainBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar

class RecognitionMain : Fragment() {

    private lateinit var binding: FragmentRecognitionMainBinding
    private lateinit var menuHandler: MenuToolbar
    private val model: MainViewModel by activityViewModels()

    lateinit var buttonWithAnimation: ConstraintLayout
    lateinit var lottieAnimationView: LottieAnimationView
    lateinit var buttonText: TextView


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
        startApp()
        infoApp()
        exitApp()

       /* binding.buttonWithAnimation.setOnClickListener{
            binding.lottieAnimationView.visibility = View.VISIBLE
            binding.lottieAnimationView.playAnimation()
            //Make TextGone
            binding.buttonText.visibility = View.GONE
            //handler
            // Simulación de carga con Handler
            Handler().postDelayed(this::resetButton , 1000) // 3 segundos de espera
        }*/
    }
    
   /* private fun resetButton() {
        //
        binding.lottieAnimationView.pauseAnimation()
        binding.lottieAnimationView.visibility = View.GONE
        binding.buttonText.visibility = View.VISIBLE
        // Navegar al fragmento principal
        findNavController().navigate(R.id.recognitionFragment)
    }*/

    private fun startApp(){
        binding.btnDetectPatofoli.setOnClickListener {
            findNavController().navigate(R.id.recognitionFragment)
        }
    }

    private fun infoApp(){
        binding.btnAbout.setOnClickListener{
            findNavController().navigate(R.id.action_recognitionMain_to_fragmentAlertDialog)
        }
    }

    private fun exitApp(){
        binding.btnExit.setOnClickListener{
            activity?.finish()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }


}