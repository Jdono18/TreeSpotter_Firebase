package com.example.treespotter_firebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TreeRecyclerViewAdapter(var trees: List<Tree>, val treeHeartListener: (Tree, Boolean) -> Unit):
    RecyclerView.Adapter<TreeRecyclerViewAdapter.ViewHolder>() {   // needs constructor

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tree: Tree) {  // defines bind function that binds 1 tree to the contents of the view
            val treeNameTextView: TextView = view.findViewById(R.id.tree_name)
            treeNameTextView.text = tree.name

            val dateSpottedTextView: TextView = view.findViewById(R.id.date_spotted)
            dateSpottedTextView.text = "${tree.dateSpotted}"

            view.findViewById<CheckBox>(R.id.heart_check).apply {
                isChecked = tree.favorite ?: false
                setOnCheckedChangeListener { checkbox,isChecked ->
                    treeHeartListener(tree, isChecked)
                }
            }


            // Also can use below code:
            //view.findViewById<TextView>(R.id.tree_name).text = tree.name  // displays tree name in TextView
            //view.findViewById<TextView>(R.id.date_spotted).text = "${tree.dateSpotted}"  // displays dateSpotted in TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {  // initializes view value that inflates a new view into fragment_tree_list layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_tree_list_item, parent, false)
        return ViewHolder(view)  // returns an instance of the ViewHolder class defined above.  That contains the view that is created from the layout that represents 1 list item
    }

    override fun getItemCount(): Int {
        return trees.size // returns size of trees list - number of trees in list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tree = trees[position]  // initializes tree val that holds the position value of the tree list
        holder.bind(tree)  // displays position for the individual tree
    }

}