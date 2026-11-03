package com.loterias.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class LegalesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_legales, container, false)

        val btnCallHelp = view.findViewById<MaterialButton>(R.id.btnCallHelp)
        btnCallHelp.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:08003330333")
                }
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Línea de Orientación: 0800-333-0333", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}
