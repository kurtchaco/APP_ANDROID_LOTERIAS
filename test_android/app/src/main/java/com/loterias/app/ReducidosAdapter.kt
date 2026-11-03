package com.loterias.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReducidosAdapter(
    private var items: List<ReducidoNumero>
) : RecyclerView.Adapter<ReducidosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvReducidoRank)
        val tvNumero: TextView = view.findViewById(R.id.tvReducidoNumero)
        val tvAmbo: TextView = view.findViewById(R.id.tvReducidoAmbo)
        val tvCentena: TextView = view.findViewById(R.id.tvReducidoCentena)
        val tvScore: TextView = view.findViewById(R.id.tvReducidoScore)
        val tvHitBadge: TextView = view.findViewById(R.id.tvReducidoHitBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reducido_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvRank.text = "#${item.rank}"
        holder.tvNumero.text = item.numero
        holder.tvAmbo.text = "Ambo: ${item.ambo}"
        holder.tvCentena.text = "C: ${item.centena}"
        holder.tvScore.text = item.score

        if (position == 0) {
            holder.tvNumero.setTextColor(ContextCompat.getColor(context, R.color.accent_gold))
            holder.tvRank.setBackgroundResource(R.drawable.bg_badge_gold)
            holder.tvRank.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            holder.tvNumero.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.tvRank.setBackgroundResource(R.drawable.bg_pill_unselected)
            holder.tvRank.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        // Hit Result Badges
        val turnosStr = if (item.turnosAcertados.isNotEmpty()) " (${item.turnosAcertados})" else ""
        when {
            item.hit4 -> {
                holder.tvHitBadge.visibility = View.VISIBLE
                holder.tvHitBadge.text = "🏆 ¡Pleno 4C! (+3.500x)$turnosStr"
                holder.tvHitBadge.setBackgroundResource(R.drawable.bg_badge_gold)
                holder.tvHitBadge.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            item.hit3 -> {
                holder.tvHitBadge.visibility = View.VISIBLE
                holder.tvHitBadge.text = "⭐ 3 Cifras (+500x)$turnosStr"
                holder.tvHitBadge.setBackgroundResource(R.drawable.bg_pill_selected)
                holder.tvHitBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            item.hit2 -> {
                holder.tvHitBadge.visibility = View.VISIBLE
                holder.tvHitBadge.text = "🎯 Ambo (+70x)$turnosStr"
                holder.tvHitBadge.setBackgroundResource(R.drawable.bg_badge_success)
                holder.tvHitBadge.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            else -> {
                holder.tvHitBadge.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReducidoNumero>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}

