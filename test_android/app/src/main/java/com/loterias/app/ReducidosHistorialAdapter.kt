package com.loterias.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReducidosHistorialAdapter(
    private var items: List<ReducidoDiaHistorial>
) : RecyclerView.Adapter<ReducidosHistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFechas: TextView = view.findViewById(R.id.tvHistorialFechas)
        val tvTurnosJugados: TextView = view.findViewById(R.id.tvHistorialTurnosJugados)
        val tvBadge: TextView = view.findViewById(R.id.tvHistorialBadge)
        val tvExtracto: TextView = view.findViewById(R.id.tvHistorialTurnosExtracto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reducido_historial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        val fechaGenStr = "${item.diaGeneracion} ${item.fechaGeneracion}"
        val primerLot = item.loterias.firstOrNull()
        val fechaDestStr = if (primerLot != null) "${primerLot.diaDestino} ${primerLot.fechaDestino}" else ""

        holder.tvFechas.text = if (fechaDestStr.isNotEmpty()) "$fechaGenStr ➔ Sorteos: $fechaDestStr" else fechaGenStr
        holder.tvTurnosJugados.text = "${item.diaTurnos} sorteos disputados"

        // Badge Status
        if (item.diaTurnos == 0) {
            holder.tvBadge.text = "⏳ Pendiente"
            holder.tvBadge.setBackgroundResource(R.drawable.bg_pill_unselected)
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        } else if (item.huboAcierto) {
            val detalleExtra = when {
                item.diaAciertos4 > 0 -> " (¡${item.diaAciertos4} Pleno 4C!)"
                item.diaAciertos3 > 0 -> " (¡${item.diaAciertos3} 3 Cifras!)"
                else -> ""
            }
            holder.tvBadge.text = "🎯 ${item.diaAciertos2} Acierto(s)$detalleExtra"
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_success)
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            holder.tvBadge.text = "0 aciertos"
            holder.tvBadge.setBackgroundResource(R.drawable.bg_pill_unselected)
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
        }

        // Build Turnos Extract String
        val extractSb = StringBuilder()
        if (item.loterias.isNotEmpty()) {
            for (lot in item.loterias) {
                if (lot.turnos.isNotEmpty()) {
                    val turnosStr = lot.turnos.joinToString(" • ") { t ->
                        val hitMark = when {
                            t.hit4 -> " 🏆 4C"
                            t.hit3 -> " ⭐ 3C"
                            t.acerto -> " 🎯"
                            else -> ""
                        }
                        val cabezaVal = if (t.cabeza.isNotEmpty()) t.cabeza else "⏳"
                        "${t.turno}: $cabezaVal$hitMark"
                    }
                    if (extractSb.isNotEmpty()) extractSb.append("\n")
                    extractSb.append("${lot.ciudad}: $turnosStr")
                }
            }
        }
        if (extractSb.isEmpty()) {
            extractSb.append("Esperando publicación de sorteos...")
        }
        holder.tvExtracto.text = extractSb.toString()
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReducidoDiaHistorial>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}

