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

class SorteosFragment : Fragment() {

    private var currentCiudad = "Jujuy"
    private var currentFecha: String? = null

    private lateinit var btnLoteriaJujuy: TextView
    private lateinit var btnLoteriaCiudad: TextView
    private lateinit var btnLoteriaProvincia: TextView

    private lateinit var rvDates: RecyclerView
    private lateinit var datesAdapter: DatesAdapter

    private lateinit var rvSorteos: RecyclerView
    private lateinit var sorteosAdapter: SorteosAdapter
    private lateinit var tvEmptySorteos: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sorteos, container, false)

        btnLoteriaJujuy = view.findViewById(R.id.btnLoteriaJujuy)
        btnLoteriaCiudad = view.findViewById(R.id.btnLoteriaCiudad)
        btnLoteriaProvincia = view.findViewById(R.id.btnLoteriaProvincia)

        rvDates = view.findViewById(R.id.rvDates)
        rvSorteos = view.findViewById(R.id.rvSorteos)
        tvEmptySorteos = view.findViewById(R.id.tvEmptySorteos)
        progressBar = view.findViewById(R.id.progressBarSorteos)
        swipeRefresh = view.findViewById(R.id.swipeRefreshSorteos)

        // Setup Dates RecyclerView (Horizontal)
        datesAdapter = DatesAdapter(emptyList(), currentFecha) { selectedDate ->
            currentFecha = selectedDate
            loadSorteos(showLoading = true)
        }
        rvDates.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = datesAdapter

        // Setup Sorteos RecyclerView
        sorteosAdapter = SorteosAdapter(emptyList())
        rvSorteos.layoutManager = LinearLayoutManager(requireContext())
        rvSorteos.adapter = sorteosAdapter

        // Switcher click listeners
        btnLoteriaJujuy.setOnClickListener { switchLoteria("Jujuy") }
        btnLoteriaCiudad.setOnClickListener { switchLoteria("Ciudad") }
        btnLoteriaProvincia.setOnClickListener { switchLoteria("Buenos Aires") }

        swipeRefresh.setColorSchemeResources(R.color.primary_blue, R.color.accent_gold)
        swipeRefresh.setOnRefreshListener {
            loadSorteos(showLoading = false)
        }

        loadSorteos(showLoading = true)

        return view
    }

    private fun switchLoteria(ciudad: String) {
        if (currentCiudad == ciudad) return
        currentCiudad = ciudad
        currentFecha = null // Reset date to load the latest date for that lottery
        updateLoteriaButtonsUI()
        loadSorteos(showLoading = true)
    }

    private fun updateLoteriaButtonsUI() {
        val selectedBg = R.drawable.bg_pill_selected
        val unselectedBg = R.drawable.bg_pill_unselected
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val muted = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        btnLoteriaJujuy.setBackgroundResource(if (currentCiudad == "Jujuy") selectedBg else unselectedBg)
        btnLoteriaJujuy.setTextColor(if (currentCiudad == "Jujuy") white else muted)

        btnLoteriaCiudad.setBackgroundResource(if (currentCiudad == "Ciudad") selectedBg else unselectedBg)
        btnLoteriaCiudad.setTextColor(if (currentCiudad == "Ciudad") white else muted)

        btnLoteriaProvincia.setBackgroundResource(if (currentCiudad == "Buenos Aires") selectedBg else unselectedBg)
        btnLoteriaProvincia.setTextColor(if (currentCiudad == "Buenos Aires") white else muted)
    }

    fun refreshData() {
        loadSorteos(showLoading = true)
    }

    private fun loadSorteos(showLoading: Boolean) {
        if (showLoading) {
            progressBar.visibility = View.VISIBLE
        }
        tvEmptySorteos.visibility = View.GONE

        val ciudadParam = currentCiudad
        val fechaParam = currentFecha

        thread {
            try {
                val (dates, turnos) = LoteriasRepository.fetchSorteos(ciudadParam, fechaParam)
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    // Si currentFecha era null, tomar la primera fecha de dates
                    if (currentFecha == null && dates.isNotEmpty()) {
                        currentFecha = dates.first()
                    }

                    datesAdapter.updateDates(dates, currentFecha)
                    sorteosAdapter.updateTurnos(turnos)

                    if (turnos.isEmpty()) {
                        tvEmptySorteos.text = "No se encontraron sorteos para esta fecha."
                        tvEmptySorteos.visibility = View.VISIBLE
                    } else {
                        tvEmptySorteos.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    if (sorteosAdapter.itemCount == 0) {
                        tvEmptySorteos.text = "Error al conectar con el servidor: ${e.message}"
                        tvEmptySorteos.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
