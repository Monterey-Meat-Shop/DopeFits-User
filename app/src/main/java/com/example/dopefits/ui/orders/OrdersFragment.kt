package com.example.dopefits.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dopefits.R
import com.example.dopefits.adapter.orders.OrdersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrdersFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("OrdersFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view)
        ordersRecyclerView.layoutManager = LinearLayoutManager(context)
        ordersAdapter = OrdersAdapter(ordersList)
        ordersRecyclerView.adapter = ordersAdapter

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Fetch orders data
        fetchOrdersData()

        return view
    }

    private fun fetchOrdersData() {
        Log.d("OrdersFragment", "fetchOrdersData called")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("OrdersFragment", "User ID is null. User might not be authenticated.")
            return
        }

        val ordersPath = "orders/$userId"
        Log.d("OrdersFragment", "Fetching orders from path: $ordersPath")

        database.child("orders").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("OrdersFragment", "onDataChange called")
                ordersList.clear()
                for (orderSnapshot in dataSnapshot.children) {
                    val orderMap = orderSnapshot.value as? Map<String, Any>
                    if (orderMap != null) {
                        val productImage = orderMap["productImage"]
                        val productImageList = if (productImage is String) {
                            listOf(productImage)
                        } else {
                            productImage as? List<String> ?: emptyList()
                        }
                        val order = Order(
                            orderId = orderMap["orderId"] as? String ?: "",
                            orderDate = orderMap["orderDate"] as? String ?: "",
                            orderStatus = orderMap["orderStatus"] as? String ?: "",
                            orderTotal = orderMap["orderTotal"] as? String ?: "",
                            productName = orderMap["productName"] as? String ?: "",
                            productImage = productImageList
                        )
                        ordersList.add(order)
                    } else {
                        Log.e("OrdersFragment", "Order data is null or not in expected format.")
                    }
                }
                ordersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("OrdersFragment", "Database error: ${databaseError.message}")
            }
        })
    }
}