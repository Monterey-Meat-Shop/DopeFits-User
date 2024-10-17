package com.example.dopefits.ui.productpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.dopefits.R
import com.example.dopefits.model.Product
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductPageFragment : Fragment() {

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = it.getParcelable("product")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_page, container, false)

        view.findViewById<TextView>(R.id.product_title).text = product.title
        view.findViewById<TextView>(R.id.product_price).text = "₱${product.price}"

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        viewPager.adapter = ImagePagerAdapter(product.picUrl)

        val dotsIndicator = view.findViewById<DotsIndicator>(R.id.dots_indicator)
        dotsIndicator.attachTo(viewPager)

        view.findViewById<Button>(R.id.add_to_cart_button).setOnClickListener {
            addToCart(product)
        }

        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            findNavController().navigateUp()
        }

        val ratingBar = view.findViewById<RatingBar>(R.id.product_rating)
        ratingBar.rating = product.stability.toFloat()

        view.findViewById<TextView>(R.id.product_brand).text = product.brand
        view.findViewById<TextView>(R.id.product_condition).text = product.condition
        view.findViewById<TextView>(R.id.product_dimensions).text = product.dimensions
        view.findViewById<TextView>(R.id.product_issue).text = product.issue
        view.findViewById<TextView>(R.id.product_size).text = product.size

        return view
    }

    private fun addToCart(product: Product) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val cartRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Cart").child(product.id.toString())
                    cartRef.setValue(product)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(requireContext(), "Item added to cart", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(requireContext(), "Failed to add item to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}