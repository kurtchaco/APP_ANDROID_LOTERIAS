package com.loterias.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import kotlin.concurrent.thread

class EfectividadFragment : Fragment() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var tvSemanaLabel: TextView
    private lateinit var cardDiagnosticBanner: MaterialCardView
    private lateinit var tvDiagnosticIcon: TextView
    private lateinit var tvDiagnosticTitle: TextView
    private lateinit var tvDiagnosticDesc: TextView

    private lateinit var lblLogrosDestacados: TextView
    private lateinit var layoutLogrosSemana: LinearLayout

    // Chips
    private lateinit var chipMod4c: TextView
    private lateinit var chipMod2Inc: TextView
    private lateinit var chipMod3c: TextView
    private lateinit var chipMod2Fijo: TextView

    // Modality details
    private lateinit var tvModalidadTitulo: TextView
    private lateinit var tvModalidadRoiBadge: TextView
    private lateinit var tvModBalance: TextView
    private lateinit var tvModInversion: TextView
    private lateinit var tvModAciertos: TextView
    private lateinit var tvModMultiplicador: TextView
    private lateinit var layoutLoteriasBreakdown: LinearLayout

    private var currentData: EfectividadCompleta? = null
    private var selectedModalityIndex: Int = 0 // 0: 4c, 1: 2c inc, 2: 3c, 3: 2c fijo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_efectividad, container, false)

        swipeRefresh = view.findViewById(R.id.swipeRefreshEfectividad)
        tvSemanaLabel = view.findViewById(R.id.tvSemanaLabel)
        cardDiagnosticBanner = view.findViewById(R.id.cardDiagnosticBanner)
        tvDiagnosticIcon = view.findViewById(R.id.tvDiagnosticIcon)
        tvDiagnosticTitle = view.findViewById(R.id.tvDiagnosticTitle)
        tvDiagnosticDesc = view.findViewById(R.id.tvDiagnosticDesc)

        lblLogrosDestacados = view.findViewById(R.id.lblLogrosDestacados)
        layoutLogrosSemana = view.findViewById(R.id.layoutLogrosSemana)

        chipMod4c = view.findViewById(R.id.chipMod4c)
        chipMod2Inc = view.findViewById(R.id.chipMod2Inc)
        chipMod3c = view.findViewById(R.id.chipMod3c)
        chipMod2Fijo = view.findViewById(R.id.chipMod2Fijo)

        tvModalidadTitulo = view.findViewById(R.id.tvModalidadTitulo)
        tvModalidadRoiBadge = view.findViewById(R.id.tvModalidadRoiBadge)
        tvModBalance = view.findViewById(R.id.tvModBalance)
        tvModInversion = view.findViewById(R.id.tvModInversion)
        tvModAciertos = view.findViewById(R.id.tvModAciertos)
        tvModMultiplicador = view.findViewById(R.id.tvModMultiplicador)
        layoutLoteriasBreakdown = view.findViewById(R.id.layoutLoteriasBreakdown)

        chipMod4c.setOnClickListener { selectModality(0) }
        chipMod2Inc.setOnClickListener { selectModality(1) }
        chipMod3c.setOnClickListener { selectModality(2) }
        chipMod2Fijo.setOnClickListener { selectModality(3) }

        swipeRefresh.setColorSchemeResources(R.color.primary_blue, R.color.accent_gold)
        swipeRefresh.setOnRefreshListener {
            loadEfectividad()
        }

        loadEfectividad()

        return view
    }

    fun refreshData() {
        loadEfectividad()
    }

    private fun loadEfectividad() {
        thread {
            try {
                val data = LoteriasRepository.fetchEfectividadCompleta()
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    swipeRefresh.isRefreshing = false
                    currentData = data
                    renderUI(data)
                }
            } catch (_: Exception) {
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun selectModality(index: Int) {
        selectedModalityIndex = index
        updateChipsUI()
        currentData?.let { renderSelectedModality(it) }
    }

    private fun updateChipsUI() {
        val selectedBg = R.drawable.bg_pill_selected
        val unselectedBg = R.drawable.bg_pill_unselected
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val muted = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        val chips = listOf(chipMod4c, chipMod2Inc, chipMod3c, chipMod2Fijo)
        for (i in chips.indices) {
            val isSel = (i == selectedModalityIndex)
            chips[i].setBackgroundResource(if (isSel) selectedBg else unselectedBg)
            chips[i].setTextColor(if (isSel) white else muted)
        }
    }

    private fun renderUI(data: EfectividadCompleta) {
        val red = ContextCompat.getColor(requireContext(), R.color.accent_red)
        val green = ContextCompat.getColor(requireContext(), R.color.accent_green)
        val redSubtle = ContextCompat.getColor(requireContext(), R.color.accent_red_subtle)
        val greenSubtle = ContextCompat.getColor(requireContext(), R.color.accent_green_subtle)

        tvSemanaLabel.text = data.semanaLabel

        // 1. Hero Diagnostic Banner
        if (data.hayVerdes) {
            cardDiagnosticBanner.setCardBackgroundColor(greenSubtle)
            cardDiagnosticBanner.strokeColor = green
            tvDiagnosticIcon.text = "🏆"
            tvDiagnosticTitle.text = data.heroTitulo
            tvDiagnosticTitle.setTextColor(green)
            tvDiagnosticDesc.text = data.heroDescripcion
        } else {
            // Solo si en NINGUNA modalidad y NINGUNA lotería hay verdes
            cardDiagnosticBanner.setCardBackgroundColor(redSubtle)
            cardDiagnosticBanner.strokeColor = red
            tvDiagnosticIcon.text = "⚠️"
            tvDiagnosticTitle.text = data.heroTitulo
            tvDiagnosticTitle.setTextColor(red)
            tvDiagnosticDesc.text = data.heroDescripcion
        }

        // 2. Renderizar Logros Destacados
        layoutLogrosSemana.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (data.hayVerdes && data.logros.isNotEmpty()) {
            lblLogrosDestacados.visibility = View.VISIBLE
            layoutLogrosSemana.visibility = View.VISIBLE

            for (logro in data.logros) {
                val cardView = inflater.inflate(R.layout.item_logro_card, layoutLogrosSemana, false)
                val tvTitulo = cardView.findViewById<TextView>(R.id.tvLogroTitulo)
                val tvRoi = cardView.findViewById<TextView>(R.id.tvLogroRoi)
                val tvDetalle = cardView.findViewById<TextView>(R.id.tvLogroDetalle)
                val tvBalance = cardView.findViewById<TextView>(R.id.tvLogroBalance)
                val tvPremio = cardView.findViewById<TextView>(R.id.tvLogroPremio)

                tvTitulo.text = logro.titulo
                tvRoi.text = logro.roi
                tvDetalle.text = logro.detalle
                tvBalance.text = logro.balance
                tvPremio.text = "Premio cobrado: ${logro.premio}"

                layoutLogrosSemana.addView(cardView)
            }
        } else {
            lblLogrosDestacados.visibility = View.GONE
            layoutLogrosSemana.visibility = View.GONE
        }

        // 3. Renderizar la modalidad seleccionada
        updateChipsUI()
        renderSelectedModality(data)
    }

    private fun renderSelectedModality(data: EfectividadCompleta) {
        if (data.modalidades.isEmpty()) return
        val mod = data.modalidades.getOrNull(selectedModalityIndex) ?: data.modalidades.first()

        val red = ContextCompat.getColor(requireContext(), R.color.accent_red)
        val green = ContextCompat.getColor(requireContext(), R.color.accent_green)

        tvModalidadTitulo.text = mod.nombre
        tvModalidadRoiBadge.text = mod.semanaRoi

        val isRoiGreen = mod.semanaRoi.contains("+") || mod.isGreen
        tvModalidadRoiBadge.setTextColor(if (isRoiGreen) green else red)
        tvModalidadRoiBadge.setBackgroundResource(if (isRoiGreen) R.drawable.bg_badge_success else R.drawable.bg_badge_danger)

        tvModBalance.text = mod.semanaBalance
        tvModBalance.setTextColor(if (mod.semanaBalance.contains("+")) green else if (mod.semanaBalance.contains("-")) red else green)

        tvModInversion.text = mod.semanaInversion
        tvModAciertos.text = "${mod.semanaAciertos} acierto(s)"

        // Multiplicador según modalidad
        when (selectedModalityIndex) {
            0 -> tvModMultiplicador.text = "3.000x ($3.000.000 por acierto)"
            1 -> tvModMultiplicador.text = "Incremental (70x)"
            2 -> tvModMultiplicador.text = "500x ($500.000 por acierto)"
            3 -> tvModMultiplicador.text = "70x ($70.000 por acierto)"
        }

        // Renderizar Desglose por Lotería
        layoutLoteriasBreakdown.removeAllViews()

        for (lot in mod.loterias) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 10, 0, 10)
            }

            val topRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val tvLotName = TextView(requireContext()).apply {
                text = "${lot.loteria} (${lot.sorteos} sorteos)"
                setTextColor(ContextCompat.getColor(context, R.color.white))
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvLotBal = TextView(requireContext()).apply {
                text = "${lot.balance} (${lot.roi})"
                setTextColor(if (lot.isGreen) green else red)
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            topRow.addView(tvLotName)
            topRow.addView(tvLotBal)
            row.addView(topRow)

            val detailText = TextView(requireContext()).apply {
                text = "Inversión: ${lot.inversion} • Aciertos: ${lot.aciertos} • Premios: ${lot.premios}"
                setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                textSize = 12f
                setPadding(0, 3, 0, 0)
            }
            row.addView(detailText)

            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                    topMargin = 10
                }
                setBackgroundColor(ContextCompat.getColor(context, R.color.border_subtle))
            }
            row.addView(divider)

            layoutLoteriasBreakdown.addView(row)
        }
    }
}
