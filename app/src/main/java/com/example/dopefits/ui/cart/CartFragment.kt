package com.example.dopefits.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dopefits.R
import com.example.dopefits.adapter.CartAdapter
import com.example.dopefits.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class CartFragment : Fragment() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var removeSelectedButton: Button
    private lateinit var proceedToCheckoutButton: Button
    private val products: MutableList<Product> = mutableListOf()
    private val productKeys: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.cart_recycler_view)
        totalPriceTextView = view.findViewById(R.id.total_price)
        removeSelectedButton = view.findViewById(R.id.remove_selected_button)
        proceedToCheckoutButton = view.findViewById(R.id.proceed_to_checkout_button)

        recyclerView.layoutManager = LinearLayoutManager(context)
        cartAdapter = CartAdapter(products, this::onItemClick) { position, button ->
            button.isEnabled = false
            removeFromCart(position) {
                cartAdapter.setRemovingFlag(position, false)
                calculateTotalPrice()
            }
        }
        recyclerView.adapter = cartAdapter

        removeSelectedButton.setOnClickListener { confirmAndRemoveSelectedItems() }
        proceedToCheckoutButton.setOnClickListener { proceedToCheckout() }

        cartAdapter.onSelectionChanged = { updateButtonStates() }

        loadCartItems()
        updateButtonStates()
        return view
    }

    private fun onItemClick(product: Product) {
        Log.d("CartFragment", "Navigating to product page for product: ${product.title}")
        val bundle = Bundle().apply {
            putParcelable("product", product)
        }
        findNavController().navigate(R.id.action_cartFragment_to_productPageFragment, bundle)
    }

    private fun loadCartItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val cartRef = database.getReference("users").child(userId).child("Cart")

            // Clear the lists before adding new items
            products.clear()
            productKeys.clear()
            cartAdapter.notifyDataSetChanged()

            cartRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val product = snapshot.getValue(Product::class.java)
                    if (product != null) {
                        products.add(product)
                        productKeys.add(snapshot.key ?: "")
                        cartAdapter.notifyItemInserted(products.size - 1)
                        calculateTotalPrice()
                        updateButtonStates()
                        Log.d("CartFragment", "Product added: ${product.title}")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val index = productKeys.indexOf(snapshot.key)
                    if (index != -1) {
                        val removedProduct = products.removeAt(index)
                        productKeys.removeAt(index)
                        cartAdapter.notifyItemRemoved(index)
                        calculateTotalPrice()
                        updateButtonStates()
                        Log.d("CartFragment", "Product removed: ${removedProduct.title}")
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartFragment", "Failed to load cart items: ${error.message}")
                }
            })
        } else {
            Log.e("CartFragment", "User not logged in")
        }
    }

    private fun removeFromCart(position: Int, onComplete: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && position in 0 until products.size) {
            val productKey = productKeys[position]
            val database = FirebaseDatabase.getInstance()
            val cartRef = database.getReference("users").child(userId).child("Cart").child(productKey)

            cartRef.removeValue().addOnSuccessListener {
                Log.d("CartFragment", "Product removed from Firebase: ${products[position].title}")
                onComplete()
            }.addOnFailureListener {
                Log.e("CartFragment", "Failed to remove product from Firebase: ${products[position].title}")
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    private fun confirmAndRemoveSelectedItems() {
        val selectedPositions = cartAdapter.getSelectedItems()
        if (selectedPositions.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Remove Items")
                .setMessage("Are you sure you want to remove the selected items from the cart?")
                .setPositiveButton("Yes") { _, _ ->
                    selectedPositions.sortedDescending().forEach { position ->
                        removeFromCart(position) {
                            cartAdapter.setRemovingFlag(position, false)
                            calculateTotalPrice()
                            updateButtonStates()
                        }
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun proceedToCheckout() {
        val selectedProducts = cartAdapter.getSelectedProducts()
        Log.d("CartFragment", "Proceeding to checkout with products: ${selectedProducts.map { it.title }}")
        // Handle checkout logic with selectedProducts
    }

    private fun calculateTotalPrice() {
        val totalPrice = cartAdapter.getSelectedProducts().sumOf { it.price }
        totalPriceTextView.text = "Total: ₱$totalPrice"
        Log.d("CartFragment", "Total price calculated: ₱$totalPrice")
    }

    private fun updateButtonStates() {
        if (!isAdded) return

        val hasSelectedItems = cartAdapter.getSelectedItems().isNotEmpty()
        removeSelectedButton.isEnabled = hasSelectedItems
        proceedToCheckoutButton.isEnabled = hasSelectedItems

        val removeButtonColor = if (hasSelectedItems) R.color.colorSecondary else R.color.colorDisabled
        val checkoutButtonColor = if (hasSelectedItems) R.color.colorPrimary else R.color.colorDisabled

        removeSelectedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), removeButtonColor)
        proceedToCheckoutButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), checkoutButtonColor)

        Log.d("CartFragment", "Button states updated: removeSelectedButton.isEnabled = $hasSelectedItems, proceedToCheckoutButton.isEnabled = $hasSelectedItems")
    }
}