package com.example.app.restaurants.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.R
import com.example.app.model.restaurantCategories.ItemCategories
import com.example.app.model.restaurantCategories.RestaurantCategories
import com.example.app.MySingleton
import com.example.app.databinding.FragmentMenuBottoSheetBinding
import com.example.app.restaurants.adapter.RestaurantMenuBottomSheetAdapter
import com.example.app.retrofit.RetroClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantMenuFragment : BottomSheetDialogFragment(),
    RestaurantMenuBottomSheetAdapter.Callback {

    private lateinit var mBinding: FragmentMenuBottoSheetBinding

    private val model: ArrayList<ItemCategories> = ArrayList()
    private val api = RetroClient.apiService
    lateinit var recyclerAdapter: RestaurantMenuBottomSheetAdapter
    private var pageCount = 1
    private var currentPage = 1
    private var restaurantCategories: Call<RestaurantCategories>? = null

    companion object {
        fun newInstance() =
            RestaurantMenuFragment().apply {
                arguments = Bundle().apply {}
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_menu_botto_sheet, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAdapter = RestaurantMenuBottomSheetAdapter(model, this)

        mBinding.rwRestaurantBottomSheet.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = recyclerAdapter
        }

        mBinding.btnMenuBottomClose.setOnClickListener {
            dismiss()
        }

        getCategory(1)
    }

    fun getCategory(position: Int) {
        restaurantCategories = api.restaurantCategories(
            position,
            MySingleton.getInstance()?.idRestaurant.toString(),
            "position"
        )
        restaurantCategories?.enqueue(object : Callback<RestaurantCategories> {
            override fun onResponse(
                call: Call<RestaurantCategories>,
                response: Response<RestaurantCategories>
            ) {
                if (response.isSuccessful) {
                    mBinding.pbCategory.visibility = View.GONE
                    recyclerAdapter.setMovieListItems(response.body()?._embedded?.item_categories!!)
                    currentPage = response.body()!!._page
                    pageCount = response.body()!!._page_count
                    Log.i(
                        "RestaurantMenuFragment",
                        "Item: ${response.body()!!._embedded.item_categories[0].name}"
                    )
                }
            }

            override fun onFailure(call: Call<RestaurantCategories>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onItemClicked(itemCategories: ItemCategories) {
        (parentFragment as RestaurantItemFragment).onAction(itemCategories.name, itemCategories.id)
        dismiss()
    }

    override fun onAllClicked(itemCategories: ItemCategories) {
        (parentFragment as RestaurantItemFragment).onAction("Все категории", "")
        dismiss()
    }

    override fun onShowLastItem() {
        if (currentPage < pageCount)
            getCategory(currentPage + 1)
    }

    override fun onStop() {
        super.onStop()
        restaurantCategories?.let { it.cancel() }
    }
}