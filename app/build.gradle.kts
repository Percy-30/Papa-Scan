plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android) // Plugin para Hilt
    id("androidx.navigation.safeargs.kotlin") // Plugin para Safe Args de Navegación
    kotlin("kapt") // Necesario para habilitar kapt en el proyecto
}

android {
    namespace = "com.example.imagerecognitionapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.imagerecognitionapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Habilitar soporte para TensorFlow Lite si es necesario
        ndk {
            //abiFilters.add("armeabi-v7a")
            //abiFilters.add("arm64-v8a")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a")//, "arm64-v8a"
            isUniversalApk = false // No generará un APK universal
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        mlModelBinding = true // Habilitar ML Model Binding
    }
}

dependencies {
    // Dependencias Core y UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ViewModel y LiveData para MVVM
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // Room para persistencia de datos
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // RecyclerView y Navegación
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Hilt para inyección de dependencias
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Pruebas unitarias y de UI
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Coroutines para operaciones asíncronas
    implementation(libs.kotlinx.coroutines.android)

    // Glide para carga de imágenes
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // CameraX para el manejo de la cámara y captura de imágenes
    implementation("androidx.camera:camera-core:1.2.3")  // Core de CameraX
    implementation("androidx.camera:camera-camera2:1.2.3")  // Dependencia para cámara 2
    implementation("androidx.camera:camera-lifecycle:1.2.3")  // Gestión del ciclo de vida de la cámara
    //implementation("androidx.camera:camera-view:1.3.0-alpha06")  // Vista para previsualización de la cámara
    // CameraX para procesamiento de imágenes en reconocimiento
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("com.github.jose-jhr:Library-CameraX:1.0.8")

    // Retrofit para manejo de API y Gson para convertir JSON a objetos de Kotlin
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.1.0")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    // TensorFlow Lite
    //implementation("org.tensorflow:litert:2.13.0")
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.2")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4") // Para tareas como visión e imagen

    // Soporte para GPU
    implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")

    // Librería Picasso para carga de imágenes
    implementation("com.squareup.picasso:picasso:2.8")

    // StyleableToast
    implementation("io.github.muddz:styleabletoast:2.4.0")



}