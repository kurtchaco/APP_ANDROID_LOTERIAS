package com.loterias.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrediccionesAdapter(
    private var turnos: List<PrediccionTurno>
) : RecyclerView.Adapter<PrediccionesAdapter.PrediccionViewHolder>() {

    fun updateTurnos(newTurnos: List<PrediccionTurno>) {
        turnos = newTurnos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrediccionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prediccion_card, parent, false)
        return PrediccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrediccionViewHolder, position: Int) {
        val item = turnos[position]
        holder.tvPredTurno.text = item.turno
        holder.tvPredDesc.text = "7 números pronosticados para este sorteo"

        holder.layoutPrediccionesRows.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemView.context)

        for (pred in item.numeros) {
            val rowView = inflater.inflate(R.layout.item_prediccion_row, holder.layoutPrediccionesRows, false)
            val tvPos = rowView.findViewById<TextView>(R.id.tvPredRowPos)
            val tvNum = rowView.findViewById<TextView>(R.id.tvPredRowNumero)
            val tv2Cifras = rowView.findViewById<TextView>(R.id.tvPredRow2Cifras)
            val tvBase = rowView.findViewById<TextView>(R.id.tvPredRowBase)
            val tvProb = rowView.findViewById<TextView>(R.id.tvPredRowProb)

            tvPos.text = "${pred.pos}"
            tvNum.text = pred.numero
            tv2Cifras.text = pred.dosCifras
            tvBase.text = pred.baseSorteo
            tvProb.text = pred.probabilidad

            holder.layoutPrediccionesRows.addView(rowView)
        }
    }

    override fun getItemCount(): Int = turnos.size

    class PrediccionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPredTurno: TextView = view.findViewById(R.id.tvPredTurno)
        val tvPredDesc: TextView = view.findViewById(R.id.tvPredDesc)
        val layoutPrediccionesRows: LinearLayout = view.findViewById(R.id.layoutPrediccionesRows)
    }
}
