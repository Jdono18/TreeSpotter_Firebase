package com.example.treespotter_firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * A simple [Fragment] subclass.
 * Use the [TreeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TreeListFragment : Fragment() {

    private val treeViewModel: TreeViewModel by lazy {  // initializes TreeViewModel as treeViewModel
        ViewModelProvider(requireActivity()).get(TreeViewModel::class.java)  // require activity from list fragment and map fragment.  Both fragments are contained in same activity.  Use same activity to access the same view model to share the same data
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val recyclerView = inflater.inflate(R.layout.fragment_tree_list, container, false)
        if (recyclerView !is RecyclerView) { throw RuntimeException("TreeListFragment should be a recycler view")}  // checks if recyclerView is actually a recyclerView if not throws an exception


        // set up user interface here
        val trees = listOf<Tree>()  // initializes a tree val that is an empty list of <Tree> objects
        val adapter = TreeRecyclerViewAdapter(trees) { tree, isFavorite ->
            treeViewModel.setIsFavorite(tree, isFavorite)
        }
        // initializes adapter
        recyclerView.layoutManager = LinearLayoutManager(context)  // sets recyclerView as a LinearLayout
        recyclerView.adapter = adapter  // assigns adapter value above to recyclerView

        treeViewModel.latestTrees.observe(requireActivity()) { treeList ->  // observes latestTrees from treeViewModel.  Receives data in treeList
            adapter.trees = treeList  // when treeList changes value in adapter is updated
            adapter.notifyDataSetChanged()  // tell adapter to update due to DataSetChange
        }
        return recyclerView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TreeListFragment()
    }
}