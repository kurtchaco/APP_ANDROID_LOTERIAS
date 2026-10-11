import os

project_dir = "/home/kurt/projects/APP_ANDROID_LOTERIAS/test_android"
dirs = [
    f"{project_dir}/app/src/main/java/com/loterias/app",
    f"{project_dir}/app/src/main/res/layout",
    f"{project_dir}/app/src/main/res/values",
]

for d in dirs:
    os.makedirs(d, exist_ok=True)

with open(f"{project_dir}/settings.gradle", "w") as f:
    f.write("rootProject.name = 'LoteriasApp'\ninclude ':app'\n")

with open(f"{project_dir}/build.gradle", "w") as f:
    f.write("""buildscript {
    ext.kotlin_version = '1.8.0'
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.0.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
""")

with open(f"{project_dir}/app/build.gradle", "w") as f:
    f.write("""plugins {
    id 'com.android.application'
    id 'kotlin-android'
}
android {
    namespace 'com.loterias.app'
    compileSdk 33
    defaultConfig {
        applicationId "com.loterias.app"
        minSdk 24
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
}
dependencies {
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Retrofit & Gson para consumir la API JSON
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
}
""")

with open(f"{project_dir}/app/src/main/AndroidManifest.xml", "w") as f:
    f.write("""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""")

with open(f"{project_dir}/app/src/main/res/values/strings.xml", "w") as f:
    f.write("""<resources>
    <string name="app_name">Loterías y Quinielas</string>
</resources>
""")

with open(f"{project_dir}/app/src/main/res/layout/activity_main.xml", "w") as f:
    f.write("""<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="#0F172A">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Resultados de Hoy (Jujuy)"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="#FFFFFF"
        android:gravity="center"
        android:layout_marginBottom="16dp"/>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <TextView
            android:id="@+id/tvResultados"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="#E2E8F0"
            android:textSize="16sp"
            android:text="Cargando sorteos desde el servidor..."/>
    </ScrollView>
</LinearLayout>
""")

with open(f"{project_dir}/app/src/main/java/com/loterias/app/MainActivity.kt", "w") as f:
    f.write("""package com.loterias.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class ApiResponse(val status: String, val data: Map<String, LoteriasData>)
data class LoteriasData(val fecha: String, val sorteos: Map<String, Any>)

interface ApiService {
    @GET("api/resultados/")
    fun getResultados(): Call<ApiResponse>
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvResultados = findViewById<TextView>(R.id.tvResultados)

        // Hacemos la peticion a tu API en Hugging Face
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kurtchaco-loterias-predicciones.hf.space/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        
        api.getResultados().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val jujuyData = response.body()!!.data["Jujuy"]
                    if (jujuyData != null) {
                        var textoSorteos = "Fecha: ${jujuyData.fecha}\\n\\n"
                        for ((turno, numeros) in jujuyData.sorteos) {
                            textoSorteos += "== $turno ==\\n$numeros\\n\\n"
                        }
                        tvResultados.text = textoSorteos
                    } else {
                        tvResultados.text = "No hay datos para Jujuy hoy."
                    }
                } else {
                    tvResultados.text = "Error al leer datos."
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                tvResultados.text = "Fallo de conexion: ${t.message}"
            }
        })
    }
}
""")
print("Android Project Scaffolding Created!")
