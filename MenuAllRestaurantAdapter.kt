package com.example.app.restaurants.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app.R
import com.example.app.databinding.ItemRestaurantMenuBinding
import com.example.app.model.restaurant.Providers
import kotlin.math.roundToInt

class MenuAllRestaurantAdapter(
    var listItem: List<Providers>,
    val callback: Callback
) : RecyclerView.Adapter<MenuAllRestaurantAdapter.MenuAllRestaurantHolder>() {

    private val list: ArrayList<Providers> = ArrayList()

    interface Callback {
        fun onItemClicked(item: Providers)
        fun onShowLastItemRestaurant()
    }

    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuAllRestaurantHolder {
        val binding: ItemRestaurantMenuBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_restaurant_menu,
            parent,
            false
        )

        return MenuAllRestaurantHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setMovieListItems(movieList: List<Providers>) {
//        this.list.clear()
        this.list.addAll(movieList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MenuAllRestaurantHolder, position: Int) {
        if (list[position].images.isNotEmpty() && list[position].images.isNotEmpty()) {
            Glide.with(holder.imageRestaurantAdapterMenuAll.context)
                .load(list[position].images[0])
                .into(holder.imageRestaurantAdapterMenuAll)
        } else {
            Glide.with(holder.imageRestaurantAdapterMenuAll.context)
                .load(R.drawable.ic_restaurant_placeholder)
                .into(holder.imageRestaurantAdapterMenuAll)
        }

        if (list[position].type == "premium") {
            holder.imageRestaurantAdapterPremiumStripe.visibility = View.VISIBLE
        }
        else{
            holder.imageRestaurantAdapterPremiumStripe.visibility = View.GONE
        }
        if (position + 1 >= list.size) {
            callback.onShowLastItemRestaurant()
        }
        holder.tvProcentRestaurantAdapterMenuAll.text = (list[position].avg_rating / 5 * 100).roundToInt()
            .toString() + " %"

        holder.itemView.setOnClickListener { callback.onItemClicked(list[position]) }
        holder.bind(list[position])
    }

    class MenuAllRestaurantHolder(binding: ItemRestaurantMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding
        val imageRestaurantAdapterMenuAll = binding.imageRestaurantAdapterMenuAll
        val imageRestaurantAdapterPremiumStripe = binding.imageRestaurantAdapterPremiumStripe
        val tvProcentRestaurantAdapterMenuAll = binding.tvProcentRestaurantAdapterMenuAll

        fun bind(providers: Providers) {
            mBinding.restaurant = providers
        }
    }
}