package com.loterias.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.concurrent.thread

class PrediccionesFragment : Fragment() {

    private var currentCiudad = "Jujuy"

    private lateinit var btnPredLoteriaJujuy: TextView
    private lateinit var btnPredLoteriaCiudad: TextView
    private lateinit var btnPredLoteriaProvincia: TextView

    private lateinit var tvPrediccionesFecha: TextView
    private lateinit var rvPredicciones: RecyclerView
    private lateinit var prediccionesAdapter: PrediccionesAdapter
    private lateinit var tvEmptyPredicciones: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_predicciones, container, false)

        btnPredLoteriaJujuy = view.findViewById(R.id.btnPredLoteriaJujuy)
        btnPredLoteriaCiudad = view.findViewById(R.id.btnPredLoteriaCiudad)
        btnPredLoteriaProvincia = view.findViewById(R.id.btnPredLoteriaProvincia)

        tvPrediccionesFecha = view.findViewById(R.id.tvPrediccionesFecha)
        rvPredicciones = view.findViewById(R.id.rvPredicciones)
        tvEmptyPredicciones = view.findViewById(R.id.tvEmptyPredicciones)
        progressBar = view.findViewById(R.id.progressBarPredicciones)
        swipeRefresh = view.findViewById(R.id.swipeRefreshPredicciones)

        // Setup RecyclerView
        prediccionesAdapter = PrediccionesAdapter(emptyList())
        rvPredicciones.layoutManager = LinearLayoutManager(requireContext())
        rvPredicciones.adapter = prediccionesAdapter

        // Switcher click listeners
        btnPredLoteriaJujuy.setOnClickListener { switchLoteria("Jujuy") }
        btnPredLoteriaCiudad.setOnClickListener { switchLoteria("Ciudad") }
        btnPredLoteriaProvincia.setOnClickListener { switchLoteria("Buenos Aires") }

        swipeRefresh.setColorSchemeResources(R.color.primary_blue, R.color.accent_gold)
        swipeRefresh.setOnRefreshListener {
            loadPredicciones(showLoading = false)
        }

        loadPredicciones(showLoading = true)

        return view
    }

    private fun switchLoteria(ciudad: String) {
        if (currentCiudad == ciudad) return
        currentCiudad = ciudad
        updateLoteriaButtonsUI()
        loadPredicciones(showLoading = true)
    }

    private fun updateLoteriaButtonsUI() {
        val selectedBg = R.drawable.bg_pill_selected
        val unselectedBg = R.drawable.bg_pill_unselected
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val muted = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        btnPredLoteriaJujuy.setBackgroundResource(if (currentCiudad == "Jujuy") selectedBg else unselectedBg)
        btnPredLoteriaJujuy.setTextColor(if (currentCiudad == "Jujuy") white else muted)

        btnPredLoteriaCiudad.setBackgroundResource(if (currentCiudad == "Ciudad") selectedBg else unselectedBg)
        btnPredLoteriaCiudad.setTextColor(if (currentCiudad == "Ciudad") white else muted)

        btnPredLoteriaProvincia.setBackgroundResource(if (currentCiudad == "Buenos Aires") selectedBg else unselectedBg)
        btnPredLoteriaProvincia.setTextColor(if (currentCiudad == "Buenos Aires") white else muted)
    }

    fun refreshData() {
        loadPredicciones(showLoading = true)
    }

    private fun loadPredicciones(showLoading: Boolean) {
        if (showLoading) {
            progressBar.visibility = View.VISIBLE
        }
        tvEmptyPredicciones.visibility = View.GONE

        val ciudadParam = currentCiudad

        thread {
            try {
                val predicciones = LoteriasRepository.fetchPredicciones(ciudadParam)
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    prediccionesAdapter.updateTurnos(predicciones)

                    if (predicciones.isNotEmpty()) {
                        val primerFecha = predicciones.first().fechaText
                        tvPrediccionesFecha.text = if (primerFecha.contains("Para", ignoreCase = true)) {
                            primerFecha.substringAfter("Para").trim()
                        } else {
                            "Hoy"
                        }
                    }

                    if (predicciones.isEmpty()) {
                        tvEmptyPredicciones.text = "No hay predicciones generadas para hoy."
                        tvEmptyPredicciones.visibility = View.VISIBLE
                    } else {
                        tvEmptyPredicciones.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    if (prediccionesAdapter.itemCount == 0) {
                        tvEmptyPredicciones.text = "Error al obtener pronósticos: ${e.message}"
                        tvEmptyPredicciones.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
