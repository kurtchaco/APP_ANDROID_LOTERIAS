package com.loterias.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class DatesAdapter(
    private var dates: List<String>,
    private var selectedDate: String?,
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<DatesAdapter.DateViewHolder>() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))

    fun updateDates(newDates: List<String>, newSelected: String?) {
        dates = newDates
        selectedDate = newSelected
        notifyDataSetChanged()
    }

    fun setSelected(date: String) {
        selectedDate = date
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_chip, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateStr = dates[position]
        val isSelected = (dateStr == selectedDate)

        var label = dateStr
        try {
            val parsed = inputFormat.parse(dateStr)
            if (parsed != null) {
                label = outputFormat.format(parsed)
            }
        } catch (_: Exception) {}

        if (position == 0) {
            label = "Hoy ($label)"
        }

        holder.tvDateChip.text = label

        if (isSelected) {
            holder.tvDateChip.setBackgroundResource(R.drawable.bg_pill_selected)
            holder.tvDateChip.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            holder.tvDateChip.setBackgroundResource(R.drawable.bg_pill_unselected)
            holder.tvDateChip.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
        }

        holder.itemView.setOnClickListener {
            selectedDate = dateStr
            notifyDataSetChanged()
            onDateClick(dateStr)
        }
    }

    override fun getItemCount(): Int = dates.size

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateChip: TextView = view.findViewById(R.id.tvDateChip)
    }
}
