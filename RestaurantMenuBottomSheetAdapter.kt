package com.example.app.restaurants.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.databinding.ItemMenuBinding
import com.example.app.model.restaurantCategories.ItemCategories
import kotlinx.android.synthetic.main.item_menu.view.*

class RestaurantMenuBottomSheetAdapter(
    var itemList: List<ItemCategories>,
    val callbacks: Callback
) : RecyclerView.Adapter<RestaurantMenuBottomSheetAdapter.RestaurantMenuBottomSheetHolder>() {
    private val list: ArrayList<ItemCategories> = ArrayList()

    init {
        list.addAll(itemList)
    }

    interface Callback {
        fun onItemClicked(itemCategories: ItemCategories)
        fun onAllClicked(itemCategories: ItemCategories)
        fun onShowLastItem()
    }

    fun setMovieListItems(movieList: List<ItemCategories>) {
        this.list.addAll(movieList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantMenuBottomSheetHolder {
        val binding: ItemMenuBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_menu,
            parent,
            false
        )

        return RestaurantMenuBottomSheetHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RestaurantMenuBottomSheetHolder, position: Int) {
        if (position == 0) {
            holder.tvMenuItemAll.visibility = View.VISIBLE
            holder.tvMenuItem.visibility = View.GONE
            holder.tvMenuItemAll.setOnClickListener { callbacks.onAllClicked(list[position]) }
        }else {
            holder.tvMenuItemAll.visibility = View.GONE
            holder.tvMenuItem.visibility = View.VISIBLE
            holder.bind(list[position - 1])
            holder.itemView.setOnClickListener { callbacks.onItemClicked(list[position - 1]) }
        }
    }

    class RestaurantMenuBottomSheetHolder(binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding
        val tvMenuItem = binding.tvMenuItem
        val tvMenuItemAll = binding.tvMenuItemAll

        fun bind(itemCategories: ItemCategories) {
            mBinding.menuBottomSheet = itemCategories
        }
    }
}