package com.example.test_bai2.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_bai2.Model.Package
import com.example.test_bai2.R

class PackageAdapter(private val packageList: List<Package>) :
    RecyclerView.Adapter<PackageAdapter.PackageViewHolder>() {

    private var selectedPosition = 0

    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutItem: LinearLayout = itemView.findViewById(R.id.layoutItem)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtBenefits: TextView = itemView.findViewById(R.id.txtBenefits)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)
        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val pkg = packageList[position]

        holder.txtName.text = pkg.name
        holder.txtBenefits.text = pkg.benefitsText
        holder.txtPrice.text = String.format("%,dđ", pkg.price)

        if (selectedPosition == position) {
            holder.layoutItem.setBackgroundResource(R.drawable.bg_button_solid)
            holder.txtName.setTextColor(Color.WHITE)
            holder.txtBenefits.setTextColor(Color.WHITE)
            holder.txtPrice.setTextColor(Color.WHITE)
        } else {
            holder.layoutItem.setBackgroundResource(R.drawable.bg_coin_item)
            holder.txtName.setTextColor(Color.parseColor("#333333"))
            holder.txtBenefits.setTextColor(Color.parseColor("#757575"))
            holder.txtPrice.setTextColor(Color.parseColor("#FF5722"))
        }

        holder.itemView.setOnClickListener {
            val old = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(old)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = packageList.size

    fun getSelectedPackage(): Package? {
        return if (selectedPosition in packageList.indices) packageList[selectedPosition] else null
    }
}