package com.example.imagerecognitionapp.data.diseaseName

data class DiseaseInfo(
    val name: String,
    val description: String,
    val prevention: String,
    val treatment: String,
    val causes: String
)

    val diseaseDatabase = mapOf(
    "Early_Blight" to DiseaseInfo(
        name = "Enfermedad 1",
        description = "Esta es la descripción de la Enfermedad 1.",
        prevention = "Evita el contacto con personas infectadas.",
        causes = "Causas Early_Blight",
        treatment = "Usa medicamentos prescritos y mantén reposo."
    ),
    "Healthy" to DiseaseInfo(
        name = "Enfermedad 2",
        description = "Descripción de la Enfermedad 2.",
        prevention = "Lávate las manos con frecuencia.",
        causes = "Causas Healthy",
        treatment = "Consulta a un médico para el tratamiento adecuado."
    ),
    "Late_Blight" to DiseaseInfo(
        name = "Enfermedad/ 3",
        description = "Descripción de la enfermedad ejemplo...",
        prevention = "Evitar factores de riesgo conocidos.",
        causes = "Causas Late_Blight",
        treatment = "Seguir las indicaciones médicas."
    )
)