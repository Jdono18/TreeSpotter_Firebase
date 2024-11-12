package com.example.treespotter_firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "TREE_VIEW_MODEL"

class TreeViewModel: ViewModel() {

    // connect to firebase

    private val db = Firebase.firestore
    private val treeCollectionReference = db.collection("trees")

    val latestTrees = MutableLiveData<List<Tree>>()  // initializes latestTrees value that is a Mutable Live Data Type containing a List of Tree Objects  () constructor at the end - makes new object

    // To get all of the tree sightings.  Connect to firebase - query for the latest 10 trees, when trees are modified we will get the most recent data

    private val latestTreesListener = treeCollectionReference  // initializes new val latestTreesListener that is a treeCollectionReference
        .orderBy("dateSpotted", Query.Direction.DESCENDING)  // orders by dataSpotted descending dates
        .limit(10)  // limits to 10 most recent records
        .addSnapshotListener { snapshot, error ->  // snapshot listener takes 2 arguments snapshot (data from database) and an error object
            if (error != null) {  // error handling
                Log.e(TAG, "Error fetching latest trees", error)
            }
            else if (snapshot != null) {  // converts snapshot to tree objects
//                val trees = snapshot.toObjects(Tree::class.java)
                val trees = mutableListOf<Tree>()
                for (treeDocument in snapshot) {  // looping over the list of documents
                    val tree = treeDocument.toObject(Tree::class.java)  // creating each document individually to a tree object
                    tree.documentReference = treeDocument.reference // setting the objects reference to the reference in the firebase database
                    trees.add(tree)  // adding the tree to the list of trees
                }
                Log.d(TAG, "Trees from firebase: $trees")  // debugging message
                latestTrees.postValue(trees)  // updates mutable live data value - latest trees.  postValue required because this is a background task.
            }
        }

    fun setIsFavorite(tree: Tree, favorite: Boolean) {
        Log.d(TAG, "Updating tree $tree to favorite $favorite")
        tree.documentReference?.update("favorite", favorite)
    }

    fun addTree(tree: Tree) {
        treeCollectionReference.add(tree)
            .addOnSuccessListener { treeDocumentReference ->
                Log.d(TAG, "New tree added at ${treeDocumentReference.path}")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error adding tree $tree", error)
            }
    }

    fun deleteTree(tree: Tree) {
        tree.documentReference?.delete()
    }
}