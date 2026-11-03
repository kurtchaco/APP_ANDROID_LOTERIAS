package com.loterias.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SorteosAdapter(
    private var turnos: List<SorteoTurno>
) : RecyclerView.Adapter<SorteosAdapter.SorteoViewHolder>() {

    fun updateTurnos(newTurnos: List<SorteoTurno>) {
        turnos = newTurnos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SorteoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sorteo_card, parent, false)
        return SorteoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SorteoViewHolder, position: Int) {
        val item = turnos[position]
        holder.tvTurnoName.text = item.turno

        if (!item.sueno.isNullOrEmpty()) {
            holder.tvSuenoBadge.visibility = View.VISIBLE
            holder.tvSuenoBadge.text = item.sueno
        } else {
            holder.tvSuenoBadge.visibility = View.GONE
        }

        // Primer Premio (Posición 1)
        val num1 = item.numeros.find { it.posicion == 1 }?.numero ?: "----"
        holder.tvPrimerPremioNum.text = num1

        // Grid 20 números (1..10 a la izquierda, 11..20 a la derecha)
        holder.layoutNumerosGrid.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemView.context)

        for (i in 1..10) {
            val leftPos = i
            val rightPos = i + 10

            val leftNum = item.numeros.find { it.posicion == leftPos }?.numero ?: "----"
            val rightNum = item.numeros.find { it.posicion == rightPos }?.numero ?: "----"

            val rowView = inflater.inflate(R.layout.item_sorteo_row, holder.layoutNumerosGrid, false)
            val tvPosLeft = rowView.findViewById<TextView>(R.id.tvPosLeft)
            val tvNumLeft = rowView.findViewById<TextView>(R.id.tvNumLeft)
            val tvPosRight = rowView.findViewById<TextView>(R.id.tvPosRight)
            val tvNumRight = rowView.findViewById<TextView>(R.id.tvNumRight)

            tvPosLeft.text = "${leftPos}º"
            tvNumLeft.text = leftNum
            tvPosRight.text = "${rightPos}º"
            tvNumRight.text = rightNum

            // Destacar si es el 1er premio
            if (leftPos == 1) {
                tvPosLeft.setTextColor(holder.itemView.context.getColor(R.color.accent_gold))
                tvNumLeft.setTextColor(holder.itemView.context.getColor(R.color.accent_gold))
            }

            holder.layoutNumerosGrid.addView(rowView)
        }
    }

    override fun getItemCount(): Int = turnos.size

    class SorteoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTurnoName: TextView = view.findViewById(R.id.tvTurnoName)
        val tvSuenoBadge: TextView = view.findViewById(R.id.tvSuenoBadge)
        val tvPrimerPremioNum: TextView = view.findViewById(R.id.tvPrimerPremioNum)
        val layoutNumerosGrid: LinearLayout = view.findViewById(R.id.layoutNumerosGrid)
    }
}
