package com.example.test_bai2.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_bai2.Model.Product
import com.example.test_bai2.R

class ProductAdapter(
    private val context: Context,
    private val list: ArrayList<Product>
) : BaseAdapter() {

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)

        val img = view.findViewById<ImageView>(R.id.imgProduct)
        val name = view.findViewById<TextView>(R.id.txtName)
        val price = view.findViewById<TextView>(R.id.txtPrice)
        val rating = view.findViewById<RatingBar>(R.id.ratingProduct)

        val product = list[position]

        name.text = product.name
        price.text = product.price
        rating.rating = product.rating ?: 0f

        Glide.with(context)
            .load(product.image)
            .into(img)

        return view
    }
}