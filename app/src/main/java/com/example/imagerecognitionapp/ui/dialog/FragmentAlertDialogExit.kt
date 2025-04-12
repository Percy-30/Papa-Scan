package com.example.imagerecognitionapp.ui.dialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.databinding.FragmentAlertDialogExitBinding


class FragmentAlertDialogExit : DialogFragment() {
    private var _binding: FragmentAlertDialogExitBinding? = null
    private val binding get() = _binding!!

    // Propiedades configurables
    var title: String = "Título"
    var message: String = "Mensaje"
    var positiveButtonText: String = "Aceptar"
    var negativeButtonText: String = "Cancelar"
    var onPositiveClick: (() -> Unit)? = null
    var onNegativeClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertDialogExitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        with(binding) {
            infoTitle.text = title
            infoDescription.text = message

            // Configurar botón positivo (Aceptar/Salir)
            btnPositive.apply {
                text = positiveButtonText
                visibility = View.VISIBLE
                setOnClickListener {
                    onPositiveClick?.invoke()
                    dismiss()
                }
            }

            // Configurar botón negativo (Cancelar)
            btnNegative.apply {
                text = negativeButtonText
                visibility = View.VISIBLE
                setOnClickListener {
                    onNegativeClick?.invoke()
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        onPositiveClick = null
        onNegativeClick = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(
            title: String,
            message: String,
            positiveText: String,
            negativeText: String,
            onPositive: () -> Unit,
            onNegative: () -> Unit
        ): FragmentAlertDialogExit {
            return FragmentAlertDialogExit().apply {
                this.title = title
                this.message = message
                this.positiveButtonText = positiveText
                this.negativeButtonText = negativeText
                this.onPositiveClick = onPositive
                this.onNegativeClick = onNegative
            }
        }
    }
}