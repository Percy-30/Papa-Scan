package com.example.imagerecognitionapp.data.diseaseName

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.imagerecognitionapp.ui.diseaseInfo.DiseaseDetailFragment

class DiseaseInfoPagerAdapter(fragment: Fragment, private val diseaseName: String) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4 // Número de pestañas

    override fun createFragment(position: Int): Fragment {
        val section = when (position) {
            0 -> "Enfermedad"
            1 -> "Tratamiento"
            2 -> "Causas"
            3 -> "Prevención"
            else -> "Otro"
        }
        return DiseaseDetailFragment.newInstance(diseaseName, section)
    }

    /*override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DiseaseDetailFragment.newInstance(diseaseName, "Enfermedad")
            1 -> DiseaseDetailFragment.newInstance(diseaseName, "Tratamiento")
            2 -> DiseaseDetailFragment.newInstance(diseaseName, "Causas")
            3 -> DiseaseDetailFragment.newInstance(diseaseName, "Prevención")
            else -> Fragment() // Fragmento vacío por defecto
        }
    }*/
}
