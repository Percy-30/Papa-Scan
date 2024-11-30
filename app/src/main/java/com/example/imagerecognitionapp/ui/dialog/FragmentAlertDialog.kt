package com.example.imagerecognitionapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.imagerecognitionapp.databinding.FragmentAlertDialogBinding

class FragmentAlertDialog : DialogFragment (){

    private var _binding: FragmentAlertDialogBinding? = null
    private val binding get() = _binding!!

    var title: String? = null

    var description: String? = null

    var onActionClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
       // binding.infoTitle.text = title ?: "Título por defecto"
        //binding.infoDescription.text = description ?: "Descripción por defecto"
        setupButton()
    }

    private fun setupButton() {
        binding.btnInfoClose.setOnClickListener {
            onActionClicked?.invoke()
            dismiss() // Cierra el diálogo automáticamente
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FragmentAlertDialog"
    }

}
