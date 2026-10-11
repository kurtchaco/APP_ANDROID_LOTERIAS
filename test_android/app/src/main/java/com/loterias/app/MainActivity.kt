package com.loterias.app

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
                        var textoSorteos = "Fecha: ${jujuyData.fecha}\n\n"
                        for ((turno, numeros) in jujuyData.sorteos) {
                            textoSorteos += "== $turno ==\n$numeros\n\n"
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
