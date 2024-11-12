package com.example.treespotter_firebase

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import java.util.Date
import kotlinx.coroutines.*

private const val TAG = "TREE_MAP_FRAGMENT"

class TreeMapFragment : Fragment() {

    private lateinit var addTreeButton: FloatingActionButton

    private var userTreeNameInput = ""

    private var locationPermissionGranted = false

    private var moveMapToUsersLocation = false

    private var fusedLocationProvider: FusedLocationProviderClient? = null

    private var map: GoogleMap? = null

    private val treeMarkers = mutableListOf<Marker>()

    private var treeList = listOf<Tree>()

    private val treeViewModel: TreeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(TreeViewModel::class.java)
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->

        Log.d(TAG, "Google map ready")
        map = googleMap

        googleMap.setOnInfoWindowClickListener { marker ->
            val treeForMarker = marker.tag as Tree
            requestDeleteTree(treeForMarker)
        }

        updateMap()
    }

    private fun requestDeleteTree(tree: Tree) {
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.confirm_delete_tree, tree.name))
            .setPositiveButton(android.R.string.ok) { dialog, id ->
                treeViewModel.deleteTree(tree)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, id ->
                // do nothing
            }
            .create()
            .show()

    }

    private fun updateMap() {
        // draw markers

        drawTrees()

        if (locationPermissionGranted) {  // if the user has granted location permission and the map has not been moved to user location call mapToUserLocation()
            if (!moveMapToUsersLocation) {
                moveMapToUserLocation()
            }
        }
        //  draw blue dot at users location
        //  show no location message if location permission not granted
        //  or device does not have location enabled
    }

    private fun setAddTreeButtonEnabled(isEnabled: Boolean) {  // defines setAddTreeButtonEnabled function (utility function) if true button is enabled green, if false button is not enabled and grey
        addTreeButton.isClickable = isEnabled
        addTreeButton.isEnabled = isEnabled

        if (isEnabled) {
            addTreeButton.backgroundTintList = AppCompatResources.getColorStateList(requireActivity(),
                android.R.color.holo_green_light)
        } else {
            addTreeButton.backgroundTintList = AppCompatResources.getColorStateList(requireActivity(),
                android.R.color.darker_gray)
        }
    }

    private fun showSnackbar(message: String) {  // defines show Snack bar function utility function that displays a message to user
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun requestLocationPermission() {  // defines requestLocationPermission function that creates a launcher that requests user permission for user location
        // has user already granted permission?
        if (ContextCompat.checkSelfPermission(requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            Log.d(TAG, "permission already granted")
            updateMap()
            setAddTreeButtonEnabled(true)
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())  // initialize the location provider if the user has already granted permission

        } else {
            // need to ask for permission
            val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {  // if permission granted
                    Log.d(TAG, "User granted permission")
                    setAddTreeButtonEnabled(true)
                    locationPermissionGranted = true
                    fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())  // initializes location provider
                } else {  // if permission not granted
                    Log.d(TAG, "User did not grant permission")
                    setAddTreeButtonEnabled(false)
                    locationPermissionGranted = false
                    showSnackbar(getString(R.string.give_permission))
                }

                updateMap()
            }

            requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }

    @SuppressLint("MissingPermission")
    private fun moveMapToUserLocation() {  // defines function to load the map based on user location
        if (map == null) {
            return
        }

        if (locationPermissionGranted) {
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            fusedLocationProvider?.lastLocation?.addOnCompleteListener { getLocationTask ->
                val location = getLocationTask.result
                if (location != null) {
                    Log.d(TAG, "User's location $location")
                    val center = LatLng(location.latitude, location.longitude)
                    val zoomLevel = 8f
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel))
                    moveMapToUsersLocation = true
                } else {
                    showSnackbar(getString(R.string.no_location))
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_tree_map, container, false)

        addTreeButton = mainView.findViewById(R.id.add_tree)
        addTreeButton.setOnClickListener {
            // todo add tree at users location - if location permission is granted & location available
            userAddTreeName()
            addTreeAtLocation(userTreeNameInput)
            }


        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment?.getMapAsync(mapReadyCallback)

        // disable add tree button until location is available
        setAddTreeButtonEnabled(false)

        // request users permission to access device location
        requestLocationPermission()

        // draw existing trees on the map.
        treeViewModel.latestTrees.observe(requireActivity()) { latestTrees ->
            treeList = latestTrees
            drawTrees()
        }


        return mainView
    }

    @SuppressLint("MissingPermission")
    private fun addTreeAtLocation(userTreeNameInput:String) {
        if (map == null) { return }
        if (fusedLocationProvider == null) { return }
        if (!locationPermissionGranted) {
            showSnackbar(getString(R.string.grant_location_permission))
            return
        }

        fusedLocationProvider?.lastLocation?.addOnCompleteListener(requireActivity()) { locationRequestTask ->
            val location = locationRequestTask.result
            if (location != null) {
                //val userTreeNameInput = userAddTreeName()
                val treeName = userTreeNameInput
                val tree = Tree(
                    name = treeName,
                    dateSpotted = Date(),
                    location = GeoPoint(location.latitude, location.longitude)
                )
                treeViewModel.addTree(tree)
                moveMapToUserLocation()
                showSnackbar(getString(R.string.added_tree, treeName))
            } else {
                showSnackbar(getString(R.string.no_location))
            }
        }
    }

    private fun drawTrees() {
        if (map == null) { return}

        for (marker in treeMarkers) {
            marker.remove()
        }

        for (tree in treeList) {
            // make a marker for each tree and add to the map
            tree.location?.let { geoPoint ->

                val isFavorite = tree.favorite ?: false
                val iconId = if (isFavorite) R.drawable.filled_heart_small else R.drawable.tree_small

                val markerOptions = MarkerOptions()
                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    .title(tree.name)
                    .snippet("Spotted on ${tree.dateSpotted}")
                    .icon(BitmapDescriptorFactory.fromResource(iconId))

                map?.addMarker(markerOptions)?.also { marker ->
                    treeMarkers.add(marker)
                    marker.tag = tree

                }

            }

        }
    }

    private fun userAddTreeName(): String {

        val inputField = EditText(requireContext())

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter a tree name:")
            .setView(inputField)
            .setPositiveButton("OK") {_,_ ->
                val userInput = inputField.text.toString()
                userTreeNameInput = userInput
            }
            .setNegativeButton("Cancel", null)
            .show()



        return userTreeNameInput

    }

    private fun getTreeName():String {

        /*val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.tree_input_dialog, null )
        val editText = layout.findViewById<EditText>(R.id.edittext)


        AlertDialog.Builder(requireActivity())
            .setView(R.layout.tree_input_dialog)
            .setTitle("Enter tree name:")
            .setPositiveButton(android.R.string.ok){ dialog, id ->
                editText.text

            }
            .setNegativeButton(android.R.string.cancel) { dialog, id ->
                // do nothing
            }
            .create()
            .show()*/


        return listOf("Fir", "Oak", "Pine", "Redwood", "Sequoia").random() // todo ask user for name

    }

    companion object {
        @JvmStatic
        fun newInstance() = TreeMapFragment()
    }
}