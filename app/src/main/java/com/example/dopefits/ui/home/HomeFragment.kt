package com.example.dopefits.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dopefits.R
import com.example.dopefits.adapter.ProductAdapter
import com.example.dopefits.model.Product
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var database: DatabaseReference
    private lateinit var searchEditText: TextInputEditText
    private var productListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        productList = mutableListOf()
        productAdapter = ProductAdapter(productList) { product ->
            val action = HomeFragmentDirections.actionHomeFragmentToProductPageFragment(product)
            findNavController().navigate(action)
        }
        recyclerView.adapter = productAdapter

        searchEditText = view.findViewById(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        database = FirebaseDatabase.getInstance().getReference("Items")
        fetchProducts()

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "onResume called")
        fetchProducts() // Refresh the product list when the fragment resumes
    }

    override fun onPause() {
        super.onPause()
        Log.d("HomeFragment", "onPause called")
        productListener?.let { database.removeEventListener(it) } // Detach listener to avoid redundant data loading
    }

    private fun fetchProducts() {
        Log.d("HomeFragment", "fetchProducts called")
        productListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeFragment", "onDataChange called")
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                productAdapter.notifyDataSetChanged()
                Log.d("HomeFragment", "Product list updated: ${productList.size} items")
                // Display all products if search text is empty
                val searchText = searchEditText.text.toString()
                if (searchText.isEmpty()) {
                    productAdapter.updateList(productList)
                } else {
                    filter(searchText)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to load products: ${error.message}")
            }
        }
        database.addListenerForSingleValueEvent(productListener!!)
    }

    private fun filter(text: String) {
        val filteredList = productList.filter {
            it.title.contains(text, ignoreCase = true)
        }
        productAdapter.updateList(filteredList)
        Log.d("HomeFragment", "Filtered list updated: ${filteredList.size} items")
    }
}