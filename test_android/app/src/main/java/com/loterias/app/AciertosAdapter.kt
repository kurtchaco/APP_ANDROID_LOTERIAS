package com.loterias.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AciertosAdapter(
    private var diasList: List<AciertoDia>
) : RecyclerView.Adapter<AciertosAdapter.AciertoDiaViewHolder>() {

    fun updateData(newList: List<AciertoDia>) {
        diasList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AciertoDiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_acierto_dia, parent, false)
        return AciertoDiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AciertoDiaViewHolder, position: Int) {
        holder.bind(diasList[position])
    }

    override fun getItemCount(): Int = diasList.size

    class AciertoDiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAciertoDiaFecha: TextView = itemView.findViewById(R.id.tvAciertoDiaFecha)
        private val tvAciertoDiaCount: TextView = itemView.findViewById(R.id.tvAciertoDiaCount)
        private val llAciertosItemsContainer: LinearLayout = itemView.findViewById(R.id.llAciertosItemsContainer)

        fun bind(dia: AciertoDia) {
            tvAciertoDiaFecha.text = dia.fechaLabel
            tvAciertoDiaCount.text = "${dia.aciertos.size} acierto${if (dia.aciertos.size != 1) "s" else ""}"

            llAciertosItemsContainer.removeAllViews()
            val inflater = LayoutInflater.from(itemView.context)

            dia.aciertos.forEachIndexed { index, acierto ->
                val rowView = inflater.inflate(R.layout.item_acierto_row, llAciertosItemsContainer, false)

                val tvRowTurno = rowView.findViewById<TextView>(R.id.tvRowTurno)
                val tvRowCifras = rowView.findViewById<TextView>(R.id.tvRowCifras)
                val tvRowNumeroSalido = rowView.findViewById<TextView>(R.id.tvRowNumeroSalido)
                val tvRowPredInfo = rowView.findViewById<TextView>(R.id.tvRowPredInfo)
                val rowDivider = rowView.findViewById<View>(R.id.rowDivider)

                tvRowTurno.text = acierto.turno
                tvRowCifras.text = acierto.cifrasBadge
                tvRowNumeroSalido.text = acierto.numeroSalido
                tvRowPredInfo.text = acierto.prediccionInfo

                if (index == dia.aciertos.size - 1) {
                    rowDivider.visibility = View.GONE
                } else {
                    rowDivider.visibility = View.VISIBLE
                }

                llAciertosItemsContainer.addView(rowView)
            }
        }
    }
}
