package com.loterias.app

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val reducidosFragment = ReducidosFragment()
    private val sorteosFragment = SorteosFragment()
    private val prediccionesFragment = PrediccionesFragment()
    private val aciertosFragment = AciertosFragment()
    private val efectividadFragment = EfectividadFragment()
    private val legalesFragment = LegalesFragment()

    private var activeFragment: Fragment = reducidosFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val btnRefresh = findViewById<ImageButton>(R.id.btnRefresh)
        val btnInfo = findViewById<ImageButton>(R.id.btnInfo)

        // Initialize fragments (Reducidos starts visible)
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, legalesFragment, "6").hide(legalesFragment)
            add(R.id.fragmentContainer, efectividadFragment, "5").hide(efectividadFragment)
            add(R.id.fragmentContainer, aciertosFragment, "4").hide(aciertosFragment)
            add(R.id.fragmentContainer, prediccionesFragment, "3").hide(prediccionesFragment)
            add(R.id.fragmentContainer, sorteosFragment, "2").hide(sorteosFragment)
            add(R.id.fragmentContainer, reducidosFragment, "1")
        }.commit()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reducidos -> {
                    switchFragment(reducidosFragment)
                    true
                }
                R.id.nav_sorteos -> {
                    switchFragment(sorteosFragment)
                    true
                }
                R.id.nav_predicciones -> {
                    switchFragment(prediccionesFragment)
                    true
                }
                R.id.nav_aciertos -> {
                    switchFragment(aciertosFragment)
                    true
                }
                R.id.nav_efectividad -> {
                    switchFragment(efectividadFragment)
                    true
                }
                else -> false
            }
        }

        btnInfo.setOnClickListener {
            switchFragment(legalesFragment)
        }

        btnRefresh.setOnClickListener {
            // Animar rotación del botón
            val rotate = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 600
                repeatCount = 0
            }
            btnRefresh.startAnimation(rotate)

            // Refrescar fragment activo
            when (activeFragment) {
                is ReducidosFragment -> (activeFragment as ReducidosFragment).refreshData()
                is SorteosFragment -> (activeFragment as SorteosFragment).refreshData()
                is PrediccionesFragment -> (activeFragment as PrediccionesFragment).refreshData()
                is AciertosFragment -> (activeFragment as AciertosFragment).refreshData()
                is EfectividadFragment -> (activeFragment as EfectividadFragment).refreshData()
            }
        }
    }

    private fun switchFragment(target: Fragment) {
        if (activeFragment != target) {
            supportFragmentManager.beginTransaction().hide(activeFragment).show(target).commit()
            activeFragment = target
        }
    }
}
