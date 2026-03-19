package com.example.test_bai2.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_bai2.Model.Voucher
import com.example.test_bai2.R

class VoucherAdapter(
    private var list: List<Voucher>,
    private val onVoucherClick: (Voucher) -> Unit
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    class VoucherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvVoucherName)
        val tvDetail: TextView = view.findViewById(R.id.tvVoucherDetail)
        val tvQty: TextView = view.findViewById(R.id.tvQuantity)
        val btnDungNgay: Button = view.findViewById(R.id.btn_dungngay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val item = list[position]

        if (item.type.lowercase() == "freeship") {
            holder.tvName.text = "Miễn phí vận chuyển"
            holder.tvDetail.text = "Áp dụng cho mọi đơn hàng"
        } else {
            holder.tvName.text = "Giảm ${String.format("%,d", item.discountAmount)}đ"
            holder.tvDetail.text = "Đơn tối thiểu ${String.format("%,d", item.minOrder)}đ"
        }

        holder.tvQty.text = "Số lượng: ${item.quantity}"

        if (item.isSelected) {
            holder.btnDungNgay.text = "Đang dùng"
            holder.btnDungNgay.alpha = 0.5f
        } else {
            holder.btnDungNgay.text = "Dùng ngay"
            holder.btnDungNgay.alpha = 1.0f
        }

        holder.btnDungNgay.setOnClickListener {
            onVoucherClick(item)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = list.size
}