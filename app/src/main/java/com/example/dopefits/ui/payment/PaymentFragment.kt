package com.example.dopefits.ui.payment

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.dopefits.R
import com.example.dopefits.ui.orders.Order
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : BaseFragment() {

    private lateinit var paymentWebView: WebView
    private lateinit var backButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        paymentWebView = view.findViewById(R.id.payment_webview)
        backButton = view.findViewById(R.id.back_button)
        setupWebView()
        setupBackButton()
        return view
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun setupWebView() {
        paymentWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.contains("success")) {
                    handlePaymentSuccess()
                    return true
                } else if (url.contains("failure")) {
                    handlePaymentFailure()
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        paymentWebView.settings.javaScriptEnabled = true

        val paymentUrl = arguments?.getString("payment_url")
        paymentUrl?.let {
            paymentWebView.loadUrl(it)
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun handlePaymentSuccess() {
        val selectedProductIds = arguments?.getStringArrayList("selected_product_ids") ?: emptyList()
        val orderTotal = arguments?.getString("order_total") ?: "0.00"
        val productName = arguments?.getString("product_name") ?: "Unknown Product"
        val productImage = arguments?.getStringArrayList("product_image") ?: listOf("")

        saveOrderToDatabase(selectedProductIds, orderTotal, productName, productImage)
        clearCart(selectedProductIds)
        AlertDialog.Builder(requireContext())
            .setTitle("Payment Successful")
            .setMessage("Your payment was successful. Thank you for your purchase!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(R.id.action_paymentFragment_to_nav_home)
            }
            .show()
    }
    private fun handlePaymentFailure() {
        AlertDialog.Builder(requireContext())
            .setTitle("Payment Failed")
            .setMessage("Your payment failed. Please try again.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .show()
    }

    private fun saveOrderToDatabase(selectedProductIds: List<String>, orderTotal: String, productName: String, productImage: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val ordersRef = database.getReference("orders").child(userId)

            val orderId = ordersRef.push().key ?: return
            val orderDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val order = Order(
                orderId = orderId,
                orderDate = orderDate,
                orderStatus = "Completed",
                orderTotal = orderTotal,
                productName = productName,
                productImage = productImage
            )

            ordersRef.child(orderId).setValue(order).addOnSuccessListener {
                // Order saved successfully
            }.addOnFailureListener {
                // Failed to save order
            }
        }
    }

    private fun clearCart(selectedProductIds: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val cartRef = database.getReference("users").child(userId).child("Cart")

            selectedProductIds.forEach { productId ->
                cartRef.child(productId).removeValue().addOnSuccessListener {
                    // Item removed successfully
                }.addOnFailureListener {
                    // Failed to remove item
                }
            }
        }
    }
}