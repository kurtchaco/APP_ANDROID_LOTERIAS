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

class ReducidosFragment : Fragment() {

    private var currentCiudad = "Jujuy"

    private lateinit var btnRedLoteriaJujuy: TextView
    private lateinit var btnRedLoteriaCiudad: TextView
    private lateinit var btnRedLoteriaProvincia: TextView

    private lateinit var tvFechaGeneracion: TextView
    private lateinit var tvFechaDestino: TextView
    private lateinit var tvAvisoDomingo: TextView
    private lateinit var tvResumenAciertosHoy: TextView
    private lateinit var tvTurnosDetalleHoy: TextView
    private lateinit var tvLegendSieteNumeros: TextView

    private lateinit var tvKpiAmbos: TextView
    private lateinit var tvKpiCentenas: TextView
    private lateinit var tvKpiPlenos: TextView

    private lateinit var rvReducidosNumeros: RecyclerView
    private lateinit var reducidosAdapter: ReducidosAdapter

    private lateinit var rvReducidosHistorial: RecyclerView
    private lateinit var historialAdapter: ReducidosHistorialAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reducidos, container, false)

        btnRedLoteriaJujuy = view.findViewById(R.id.btnRedLoteriaJujuy)
        btnRedLoteriaCiudad = view.findViewById(R.id.btnRedLoteriaCiudad)
        btnRedLoteriaProvincia = view.findViewById(R.id.btnRedLoteriaProvincia)

        tvFechaGeneracion = view.findViewById(R.id.tvFechaGeneracion)
        tvFechaDestino = view.findViewById(R.id.tvFechaDestino)
        tvAvisoDomingo = view.findViewById(R.id.tvAvisoDomingo)
        tvResumenAciertosHoy = view.findViewById(R.id.tvResumenAciertosHoy)
        tvTurnosDetalleHoy = view.findViewById(R.id.tvTurnosDetalleHoy)
        tvLegendSieteNumeros = view.findViewById(R.id.tvLegendSieteNumeros)

        tvKpiAmbos = view.findViewById(R.id.tvKpiAmbos)
        tvKpiCentenas = view.findViewById(R.id.tvKpiCentenas)
        tvKpiPlenos = view.findViewById(R.id.tvKpiPlenos)

        rvReducidosNumeros = view.findViewById(R.id.rvReducidosNumeros)
        rvReducidosHistorial = view.findViewById(R.id.rvReducidosHistorial)
        progressBar = view.findViewById(R.id.progressBarReducidos)
        swipeRefresh = view.findViewById(R.id.swipeRefreshReducidos)

        // Setup RecyclerViews
        reducidosAdapter = ReducidosAdapter(emptyList())
        rvReducidosNumeros.layoutManager = LinearLayoutManager(requireContext())
        rvReducidosNumeros.adapter = reducidosAdapter

        historialAdapter = ReducidosHistorialAdapter(emptyList())
        rvReducidosHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvReducidosHistorial.adapter = historialAdapter

        // Lottery Switchers
        btnRedLoteriaJujuy.setOnClickListener { switchLoteria("Jujuy") }
        btnRedLoteriaCiudad.setOnClickListener { switchLoteria("Ciudad") }
        btnRedLoteriaProvincia.setOnClickListener { switchLoteria("Buenos Aires") }

        swipeRefresh.setColorSchemeResources(R.color.accent_gold, R.color.primary_blue)
        swipeRefresh.setOnRefreshListener {
            loadReducidos(showLoading = false)
        }

        loadReducidos(showLoading = true)

        return view
    }

    private fun switchLoteria(ciudad: String) {
        if (currentCiudad == ciudad) return
        currentCiudad = ciudad
        updateLoteriaButtonsUI()
        loadReducidos(showLoading = true)
    }

    private fun updateLoteriaButtonsUI() {
        val selectedBg = R.drawable.bg_pill_selected
        val unselectedBg = R.drawable.bg_pill_unselected
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val muted = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        btnRedLoteriaJujuy.setBackgroundResource(if (currentCiudad == "Jujuy") selectedBg else unselectedBg)
        btnRedLoteriaJujuy.setTextColor(if (currentCiudad == "Jujuy") white else muted)

        btnRedLoteriaCiudad.setBackgroundResource(if (currentCiudad == "Ciudad") selectedBg else unselectedBg)
        btnRedLoteriaCiudad.setTextColor(if (currentCiudad == "Ciudad") white else muted)

        btnRedLoteriaProvincia.setBackgroundResource(if (currentCiudad == "Buenos Aires") selectedBg else unselectedBg)
        btnRedLoteriaProvincia.setTextColor(if (currentCiudad == "Buenos Aires") white else muted)
    }

    fun refreshData() {
        loadReducidos(showLoading = true)
    }

    private fun loadReducidos(showLoading: Boolean) {
        if (showLoading) {
            progressBar.visibility = View.VISIBLE
        }

        val ciudadParam = currentCiudad

        thread {
            try {
                val data = LoteriasRepository.fetchReducidos(ciudadParam)

                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    // Fechas
                    tvFechaGeneracion.text = "${data.diaGeneracion} ${data.fechaGeneracion}".trim()
                    tvFechaDestino.text = "${data.diaDestino} ${data.fechaDestino}".trim()
                    tvAvisoDomingo.visibility = if (data.avisoDomingo) View.VISIBLE else View.GONE

                    // Legend
                    val diaDestinoStr = if (data.diaDestino.isNotEmpty()) "${data.diaDestino} ${data.fechaDestino}" else "día siguiente"
                    tvLegendSieteNumeros.text = "✨ Los 7 números para jugar el $diaDestinoStr:"

                    // Control del día (En vivo)
                    val turnosJugados = data.turnosJugados
                    val aciertosCab = data.aciertosCabeza
                    if (turnosJugados == 0) {
                        tvResumenAciertosHoy.text = "⏳ Esperando sorteos (0/5)"
                        tvResumenAciertosHoy.setBackgroundResource(R.drawable.bg_pill_unselected)
                        tvResumenAciertosHoy.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                    } else if (aciertosCab > 0) {
                        tvResumenAciertosHoy.text = "🎯 $aciertosCab Acierto(s) ($turnosJugados/5)"
                        tvResumenAciertosHoy.setBackgroundResource(R.drawable.bg_badge_success)
                        tvResumenAciertosHoy.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    } else {
                        tvResumenAciertosHoy.text = "0 aciertos ($turnosJugados/5)"
                        tvResumenAciertosHoy.setBackgroundResource(R.drawable.bg_pill_unselected)
                        tvResumenAciertosHoy.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted))
                    }

                    // Turnos chips summary
                    if (data.turnosSorteo.isNotEmpty()) {
                        val turnosStr = data.turnosSorteo.joinToString(" • ") { t ->
                            val mark = when {
                                t.hit4 -> " 🏆"
                                t.hit3 -> " ⭐ 3C"
                                t.hit2 -> " 🎯"
                                else -> ""
                            }
                            val cabeza = if (t.publicado && t.cabeza != null) t.cabeza else "⏳"
                            "${t.turno}: $cabeza$mark"
                        }
                        tvTurnosDetalleHoy.text = turnosStr
                    } else {
                        tvTurnosDetalleHoy.text = "Previa: ⏳ • Primera: ⏳ • Matutina: ⏳ • Vespertina: ⏳ • Nocturna: ⏳"
                    }

                    // Actualizar los 7 números
                    reducidosAdapter.updateData(data.seleccionados)

                    // KPIs 10 Días
                    tvKpiAmbos.text = data.historialAciertos2.toString()
                    tvKpiCentenas.text = data.historialAciertos3.toString()
                    tvKpiPlenos.text = data.historialAciertos4.toString()

                    // Actualizar historial 10 días
                    historialAdapter.updateData(data.historialDias)
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }
}

