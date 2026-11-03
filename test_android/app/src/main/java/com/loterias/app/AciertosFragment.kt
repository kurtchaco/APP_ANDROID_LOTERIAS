package com.loterias.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.concurrent.thread

class AciertosFragment : Fragment() {

    private var currentCiudad = "Jujuy"

    private lateinit var btnLoteriaJujuy: TextView
    private lateinit var btnLoteriaCiudad: TextView
    private lateinit var btnLoteriaProvincia: TextView

    private lateinit var rvAciertos: RecyclerView
    private lateinit var aciertosAdapter: AciertosAdapter
    private lateinit var llEmptyAciertos: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aciertos, container, false)

        btnLoteriaJujuy = view.findViewById(R.id.btnAciertosLoteriaJujuy)
        btnLoteriaCiudad = view.findViewById(R.id.btnAciertosLoteriaCiudad)
        btnLoteriaProvincia = view.findViewById(R.id.btnAciertosLoteriaProvincia)

        rvAciertos = view.findViewById(R.id.rvAciertos)
        llEmptyAciertos = view.findViewById(R.id.llEmptyAciertos)
        progressBar = view.findViewById(R.id.progressBarAciertos)
        swipeRefresh = view.findViewById(R.id.swipeRefreshAciertos)

        aciertosAdapter = AciertosAdapter(emptyList())
        rvAciertos.layoutManager = LinearLayoutManager(requireContext())
        rvAciertos.adapter = aciertosAdapter

        btnLoteriaJujuy.setOnClickListener { switchLoteria("Jujuy") }
        btnLoteriaCiudad.setOnClickListener { switchLoteria("Ciudad") }
        btnLoteriaProvincia.setOnClickListener { switchLoteria("Buenos Aires") }

        swipeRefresh.setColorSchemeResources(R.color.primary_blue, R.color.accent_green)
        swipeRefresh.setOnRefreshListener {
            loadAciertos(showLoading = false)
        }

        loadAciertos(showLoading = true)

        return view
    }

    private fun switchLoteria(ciudad: String) {
        if (currentCiudad == ciudad) return
        currentCiudad = ciudad
        updateLoteriaButtonsUI()
        loadAciertos(showLoading = true)
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
        loadAciertos(showLoading = true)
    }

    private fun loadAciertos(showLoading: Boolean) {
        if (showLoading) {
            progressBar.visibility = View.VISIBLE
        }

        thread {
            try {
                val aciertosDias = LoteriasRepository.fetchAciertos(currentCiudad)

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    if (aciertosDias.isEmpty()) {
                        llEmptyAciertos.visibility = View.VISIBLE
                        rvAciertos.visibility = View.GONE
                    } else {
                        llEmptyAciertos.visibility = View.GONE
                        rvAciertos.visibility = View.VISIBLE
                        aciertosAdapter.updateData(aciertosDias)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    if (aciertosAdapter.itemCount == 0) {
                        llEmptyAciertos.visibility = View.VISIBLE
                        rvAciertos.visibility = View.GONE
                    }
                }
            }
        }
    }
}
