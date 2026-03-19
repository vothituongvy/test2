package com.example.test_bai2.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_bai2.Model.CartItem
import com.example.test_bai2.R

class CheckoutAdapter(private val list: List<CartItem>) :
    RecyclerView.Adapter<CheckoutAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val imgProduct: ImageView = v.findViewById(R.id.imgCart)
        val tvName: TextView = v.findViewById(R.id.tvCartName)
        val tvPrice: TextView = v.findViewById(R.id.tvCartPrice)
        val tvSize: TextView = v.findViewById(R.id.tvCartSize)
        val tvQty: TextView = v.findViewById(R.id.txtSheetQty)

        val cbSelect: View = v.findViewById(R.id.cbSelect)
        val btnPlus: View = v.findViewById(R.id.btnPlus)
        val btnMinus: View = v.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.tvName.text = item.name
        holder.tvSize.text = "Size: ${item.size}"
        holder.tvPrice.text = "${String.format("%,d", item.price)}đ"
        holder.tvQty.text = "x${item.quantity}"

        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.bg_search)
            .into(holder.imgProduct)


        holder.cbSelect.visibility = View.GONE
        holder.btnPlus.visibility = View.GONE
        holder.btnMinus.visibility = View.GONE
    }

    override fun getItemCount() = list.size
}