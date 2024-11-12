package com.example.treespotter_firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {

    val CURRENT_FRAGMENT_BUNDLE_KEY = "current fragment bundle key"
    var currentFragmentTag = "MAP"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentFragmentTag = savedInstanceState?.getString(CURRENT_FRAGMENT_BUNDLE_KEY) ?: "MAP"

        showFragment(currentFragmentTag)  // displays current fragment at load or defaults to MAP

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.show_map -> {
                    showFragment("MAP")
                    true
                }
                R.id.show_list -> {
                    showFragment("LIST")
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun showFragment(tag: String) {
        // if we are not seeing the fragment with the given tag, display it

        currentFragmentTag = tag

        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            val transaction =
                supportFragmentManager.beginTransaction() // starting a transaction
            when (tag) {
                "MAP" -> transaction.replace(
                    R.id.fragmentContainerView,
                    TreeMapFragment.newInstance(),
                    "MAP"
                )

                "LIST" -> transaction.replace(
                    R.id.fragmentContainerView,
                    TreeListFragment.newInstance(),
                    "LIST"
                )
            }
            transaction.commit()  // makes the change

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_BUNDLE_KEY, currentFragmentTag)
    }
}




