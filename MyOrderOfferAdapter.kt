package com.example.app.my_orders.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.model.offer.CustomerOffers
import kotlinx.android.synthetic.main.item_my_order.view.*

class MyOrderOfferAdapter(var list: List<CustomerOffers>, val callback: Callback) :
    RecyclerView.Adapter<MyOrderOfferAdapter.MyOrderOfferHolder>() {

    interface Callback {
        fun onItemClicked(customerOffers: CustomerOffers)
    }

    fun setMovieListItems(movieList: List<CustomerOffers>) {
        this.list = movieList;
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrderOfferHolder {
        return MyOrderOfferHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_order, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyOrderOfferHolder, position: Int) {
        holder.tvMyOrderNumberOrder.text = "# " + list[position].order_number.toString()
        holder.tvMyOrderCreateData.text = "Что угодно"

        val context = holder.tvMyOrderDetailsOrder.context

        holder.tvMyOrderSum.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener { callback.onItemClicked(list[position]) }

        when (list[position].status) {
            "preparing" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_error
                    )
                )
            }
            "pending" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_orange_3
                    )
                )
                holder.tvMyOrderDetailsOrder.text = "Приготовление заказа"
            }
            "processing" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_orange_3
                    )
                )
                holder.tvMyOrderDetailsOrder.text = "В обработке"
            }
            "confirmed" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_green
                    )
                )
                holder.tvMyOrderDetailsOrder.text = "Заказ принят"
            }
            "sent" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_orange_3
                    )
                )
                holder.tvMyOrderDetailsOrder.text = "Курьер в пути"
            }
            "finished" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_green
                    )
                )
                holder.tvMyOrderDetailsOrder.text = "Выполнен"
            }
            "delivered" -> {
                holder.tvMyOrderDetailsOrder.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_green
                    )
                )
                holder.tvMyOrderDetailsOrder.text = ""
            }
        }
    }

    class MyOrderOfferHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMyOrderNumberOrder = view.tvMyOrderNumberOrder
        val tvMyOrderDetailsOrder = view.tvMyOrderDetailsOrder
        val tvMyOrderCreateData = view.tvMyOrderCreateData
        val tvMyOrderSum = view.tvMyOrderSum
    }
}