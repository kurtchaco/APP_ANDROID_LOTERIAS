package com.loterias.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import kotlin.concurrent.thread

class ReducidosFragment : Fragment() {

    private var currentCiudad = "Jujuy"
    private var activeData: ReducidosData? = null

    private lateinit var btnRedLoteriaJujuy: TextView
    private lateinit var btnRedLoteriaCiudad: TextView
    private lateinit var btnRedLoteriaProvincia: TextView

    private lateinit var cardBannerHistorico: MaterialCardView
    private lateinit var tvBannerHistorico: TextView
    private lateinit var btnVolverAHoy: TextView

    private lateinit var tvFechaGeneracion: TextView
    private lateinit var tvFechaDestino: TextView
    private lateinit var tvAvisoDomingo: TextView
    private lateinit var tvResumenAciertosHoy: TextView
    private lateinit var tvTurnosDetalleHoy: TextView
    private lateinit var tvLegendSieteNumeros: TextView

    // Card Explicación Expandible
    private lateinit var cardExplicacionReducidos: MaterialCardView
    private lateinit var tvBadgeExpandir: TextView
    private lateinit var tvSubtituloExplicacion: TextView
    private lateinit var layoutContenidoExplicacion: View
    private lateinit var btnAgrandarModal: TextView
    private lateinit var btnContraerExplicacion: TextView

    private lateinit var tvKpiAmbos: TextView
    private lateinit var tvKpiCentenas: TextView
    private lateinit var tvKpiPlenos: TextView

    private lateinit var rvReducidosNumeros: RecyclerView
    private lateinit var reducidosAdapter: ReducidosAdapter

    private lateinit var rvReducidosHistorial: RecyclerView
    private lateinit var historialAdapter: ReducidosHistorialAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var scrollView: NestedScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reducidos, container, false)

        btnRedLoteriaJujuy = view.findViewById(R.id.btnRedLoteriaJujuy)
        btnRedLoteriaCiudad = view.findViewById(R.id.btnRedLoteriaCiudad)
        btnRedLoteriaProvincia = view.findViewById(R.id.btnRedLoteriaProvincia)

        cardBannerHistorico = view.findViewById(R.id.cardBannerHistorico)
        tvBannerHistorico = view.findViewById(R.id.tvBannerHistorico)
        btnVolverAHoy = view.findViewById(R.id.btnVolverAHoy)

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

        // Find parent NestedScrollView
        var parentView = rvReducidosNumeros.parent
        while (parentView != null && parentView !is NestedScrollView) {
            parentView = parentView.parent
        }
        if (parentView is NestedScrollView) {
            scrollView = parentView
        }

        // Card Explicación Expandible
        cardExplicacionReducidos = view.findViewById(R.id.cardExplicacionReducidos)
        tvBadgeExpandir = view.findViewById(R.id.tvBadgeExpandir)
        tvSubtituloExplicacion = view.findViewById(R.id.tvSubtituloExplicacion)
        layoutContenidoExplicacion = view.findViewById(R.id.layoutContenidoExplicacion)
        btnAgrandarModal = view.findViewById(R.id.btnAgrandarModal)
        btnContraerExplicacion = view.findViewById(R.id.btnContraerExplicacion)

        val toggleAction = View.OnClickListener {
            toggleExplicacionCard()
        }
        cardExplicacionReducidos.setOnClickListener(toggleAction)
        btnContraerExplicacion.setOnClickListener(toggleAction)

        btnAgrandarModal.setOnClickListener {
            mostrarModalExplicacionAgrandada()
        }

        // Setup RecyclerViews
        reducidosAdapter = ReducidosAdapter(emptyList())
        rvReducidosNumeros.layoutManager = LinearLayoutManager(requireContext())
        rvReducidosNumeros.adapter = reducidosAdapter

        historialAdapter = ReducidosHistorialAdapter(emptyList()) { dia, loteria ->
            mostrarDiaHistorico(dia, loteria)
        }
        rvReducidosHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvReducidosHistorial.adapter = historialAdapter

        // Lottery Switchers
        btnRedLoteriaJujuy.setOnClickListener { switchLoteria("Jujuy") }
        btnRedLoteriaCiudad.setOnClickListener { switchLoteria("Ciudad") }
        btnRedLoteriaProvincia.setOnClickListener { switchLoteria("Buenos Aires") }

        // Banner Action: Volver a hoy
        val volverAction = View.OnClickListener {
            restaurarDiaActivo()
        }
        cardBannerHistorico.setOnClickListener(volverAction)
        btnVolverAHoy.setOnClickListener(volverAction)

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
        cardBannerHistorico.visibility = View.GONE
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

    private fun toggleExplicacionCard() {
        if (layoutContenidoExplicacion.visibility == View.VISIBLE) {
            layoutContenidoExplicacion.visibility = View.GONE
            tvBadgeExpandir.text = "Ver ▾"
            tvSubtituloExplicacion.text = "Toca para ver cómo optimiza tu jugada (-80%)"
        } else {
            layoutContenidoExplicacion.visibility = View.VISIBLE
            tvBadgeExpandir.text = "Contraer ▴"
            tvSubtituloExplicacion.text = "Explicación activa (toca para contraer)"
        }
    }

    private fun mostrarModalExplicacionAgrandada() {
        try {
            val context = requireContext()
            val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_explicacion_reducidos, null)

            val btnCerrar = dialogView.findViewById<TextView>(R.id.btnCerrarDialogExplicacion)
            btnCerrar?.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setContentView(dialogView)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

                    activeData = data
                    cardBannerHistorico.visibility = View.GONE
                    renderDataEnVistaPrincipal(data)

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

    private fun renderDataEnVistaPrincipal(data: ReducidosData) {
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
    }

    private fun mostrarDiaHistorico(dia: ReducidoDiaHistorial, loteriaOpt: ReducidoHistorialLoteria?) {
        val loteria = loteriaOpt ?: dia.loterias.firstOrNull {
            it.ciudad.equals(currentCiudad, ignoreCase = true)
        } ?: dia.loterias.firstOrNull() ?: return

        cardBannerHistorico.visibility = View.VISIBLE
        tvBannerHistorico.text = "📅 Histórico: Generado ${dia.diaGeneracion} ${dia.fechaGeneracion} ➔ Sorteos: ${loteria.diaDestino} ${loteria.fechaDestino}"

        tvFechaGeneracion.text = "${dia.diaGeneracion} ${dia.fechaGeneracion}".trim()
        tvFechaDestino.text = "${loteria.diaDestino} ${loteria.fechaDestino}".trim()
        tvAvisoDomingo.visibility = View.GONE

        tvLegendSieteNumeros.text = "✨ Los 7 números jugados el ${loteria.diaDestino} ${loteria.fechaDestino}:"

        // Resumen aciertos
        if (loteria.turnosJugados == 0) {
            tvResumenAciertosHoy.text = "⏳ Sorteos aún no disputados"
            tvResumenAciertosHoy.setBackgroundResource(R.drawable.bg_pill_unselected)
            tvResumenAciertosHoy.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        } else if (loteria.aciertosCabeza > 0) {
            tvResumenAciertosHoy.text = "🎯 ${loteria.aciertosCabeza} Acierto(s) (${loteria.turnosJugados}/5)"
            tvResumenAciertosHoy.setBackgroundResource(R.drawable.bg_badge_success)
            tvResumenAciertosHoy.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        } else {
            tvResumenAciertosHoy.text = "0 aciertos (${loteria.turnosJugados}/5)"
            tvResumenAciertosHoy.setBackgroundResource(R.drawable.bg_pill_unselected)
            tvResumenAciertosHoy.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted))
        }

        // Turnos extract
        if (loteria.turnos.isNotEmpty()) {
            val turnosStr = loteria.turnos.joinToString(" • ") { t ->
                val mark = when {
                    t.hit4 -> " 🏆"
                    t.hit3 -> " ⭐ 3C"
                    t.acerto -> " 🎯"
                    else -> ""
                }
                val cabeza = if (t.cabeza.isNotEmpty()) t.cabeza else "⏳"
                "${t.turno}: $cabeza$mark"
            }
            tvTurnosDetalleHoy.text = turnosStr
        }

        // Actualizar números con los números de ese día
        if (loteria.seleccionados.isNotEmpty()) {
            reducidosAdapter.updateData(loteria.seleccionados)
        }

        // Scroll suave hacia arriba
        if (::scrollView.isInitialized) {
            scrollView.smoothScrollTo(0, 0)
        }
    }

    private fun restaurarDiaActivo() {
        cardBannerHistorico.visibility = View.GONE
        activeData?.let {
            renderDataEnVistaPrincipal(it)
        }
    }
}
