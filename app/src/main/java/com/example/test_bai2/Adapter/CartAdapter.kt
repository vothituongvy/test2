package com.example.test_bai2.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_bai2.Model.CartItem
import com.example.test_bai2.R

class CartAdapter(
    private val cartList: List<CartItem>,
    private val onQuantityChange: (String, Int) -> Unit,
    private val onSelectionChange: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {


    val selectedItems = mutableMapOf<String, Boolean>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgCart)
        val name: TextView = view.findViewById(R.id.tvCartName)
        val size: TextView = view.findViewById(R.id.tvCartSize)
        val price: TextView = view.findViewById(R.id.tvCartPrice)
        val qty: TextView = view.findViewById(R.id.txtSheetQty)
        val cb: CheckBox = view.findViewById(R.id.cbSelect)
        val btnMinus: ImageButton = view.findViewById(R.id.btnMinus)
        val btnPlus: ImageButton = view.findViewById(R.id.btnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartList[position]

        holder.name.text = item.name
        holder.size.text = "Size: ${item.size}"
        holder.price.text = "${String.format("%,d", item.price)}đ"
        holder.qty.text = item.quantity.toString()
        Glide.with(holder.itemView.context).load(item.image).into(holder.img)


        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = selectedItems[item.id] ?: false
        holder.cb.setOnCheckedChangeListener { _, isChecked ->
            selectedItems[item.id] = isChecked
            onSelectionChange()
        }

        holder.btnPlus.setOnClickListener { onQuantityChange(item.id, item.quantity + 1) }
        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) onQuantityChange(item.id, item.quantity - 1)
        }
    }

    override fun getItemCount() = cartList.size

    fun getTotalPrice(): Long {
        var total = 0L
        cartList.forEach { if (selectedItems[it.id] == true) total += it.price * it.quantity }
        return total
    }
}