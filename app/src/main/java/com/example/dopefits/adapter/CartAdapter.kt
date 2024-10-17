package com.example.dopefits.adapter

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dopefits.R
import com.example.dopefits.model.Product
import java.net.HttpURLConnection
import java.net.URL

class CartAdapter(
    private var products: MutableList<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onRemoveClick: (Int, Button) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val isRemoving = mutableMapOf<Int, Boolean>()
    private val selectedItems = mutableSetOf<Int>()
    var onSelectionChanged: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, position, onItemClick, onRemoveClick, isRemoving, selectedItems) {
            onSelectionChanged?.invoke()
        }
    }

    override fun getItemCount(): Int = products.size

    fun setRemovingFlag(position: Int, value: Boolean) {
        isRemoving[position] = value
    }

    fun getSelectedItems(): List<Int> {
        return selectedItems.filter { it in 0 until products.size }
    }

    fun getSelectedProducts(): List<Product> {
        return getSelectedItems().map { products[it] }
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val titleTextView: TextView = itemView.findViewById(R.id.product_name)
        private val priceTextView: TextView = itemView.findViewById(R.id.product_price)
        private val removeButton: Button = itemView.findViewById(R.id.remove_button)
        private val selectCheckBox: CheckBox = itemView.findViewById(R.id.select_checkbox)

        fun bind(
            product: Product,
            position: Int,
            onItemClick: (Product) -> Unit,
            onRemoveClick: (Int, Button) -> Unit,
            isRemoving: MutableMap<Int, Boolean>,
            selectedItems: MutableSet<Int>,
            onSelectionChanged: () -> Unit
        ) {
            titleTextView.text = product.title
            priceTextView.text = product.price.toString()
            selectCheckBox.isChecked = selectedItems.contains(position)

            if (product.picUrl.isNotEmpty()) {
                val imageUrl = product.picUrl[0]
                Thread {
                    try {
                        val url = URL(imageUrl)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.doInput = true
                        connection.connect()
                        val inputStream = connection.inputStream
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        itemView.post {
                            productImage.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("CartViewHolder", "Error loading image: ${e.message}")
                    }
                }.start()
            }

            itemView.setOnClickListener {
                Log.d("CartAdapter", "Item clicked: ${product.title}")
                onItemClick(product)
            }
            productImage.setOnClickListener {
                Log.d("CartAdapter", "Product image clicked: ${product.title}")
                onItemClick(product)
            }

            removeButton.setOnClickListener {
                if (isRemoving[position] != true) {
                    Log.d("CartAdapter", "Remove button clicked for position: $position")
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item from the cart?")
                        .setPositiveButton("Yes") { _, _ ->
                            isRemoving[position] = true
                            onRemoveClick(position, removeButton)
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }

            selectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(position)
                } else {
                    selectedItems.remove(position)
                }
                Log.d("CartAdapter", "Selection changed for position: $position, isChecked: $isChecked")
                onSelectionChanged()
            }
        }
    }
}