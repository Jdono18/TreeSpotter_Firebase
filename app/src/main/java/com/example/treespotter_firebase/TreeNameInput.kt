package com.example.treespotter_firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val USER_TREE = "user-tree-name-input"


/**
 * A simple [Fragment] subclass.
 * Use the [TreeNameInput.newInstance] factory method to
 * create an instance of this fragment.
 */
class TreeNameInput : Fragment() {

    private lateinit var userTreeInput: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_tree_name_input, container, false)

        userTreeInput = view.findViewById(R.id.edittext)
        submitButton = view.findViewById(R.id.submitButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        submitButton.setOnClickListener {

            val treeName = userTreeInput.text.toString()
            if (treeName.isBlank()){
                Toast.makeText(context,"Please add a tree name!",Toast.LENGTH_SHORT).show()
            } else

            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, TreeMapFragment.newInstance(treeName), "USER_TREE").commit()
            }

        cancelButton.setOnClickListener {
            val treeName: String? = null
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, TreeMapFragment.newInstance(treeName), "USER_TREE").commit()
        }
        return view  // Inflate the layout for this fragment
        }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param treeName Parameter 1.
         * @return A new instance of fragment TreeNameInput.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = TreeNameInput()

        }
    }
