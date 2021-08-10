package com.example.app.my_orders.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.MySingleton
import com.example.app.R
import com.example.app.databinding.FragmentMyOrderOrderBinding
import com.example.app.model.myOrder.order.MyOrderOrder
import com.example.app.model.myOrder.order.Orders
import com.example.app.my_orders.adapter.MyOrderOrderAdapter
import com.example.app.retrofit.RetroClient
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil

class OrderFragment : Fragment(), MyOrderOrderAdapter.Callback {
    private lateinit var mBinding: FragmentMyOrderOrderBinding

    private val api = RetroClient.apiService
    lateinit var recyclerAdapter: MyOrderOrderAdapter
    private val model: ArrayList<Orders> = ArrayList()
    var pageCount: Int = 1
    var currentPage: Int = 1
    private var getOrder: Call<MyOrderOrder>? = null
    private var getOrderPage: Call<MyOrderOrder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_order_order,
            container,
            false
        )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerAdapter = MyOrderOrderAdapter(model, this)


        mBinding.rwMyOrder.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = recyclerAdapter
        }

        getOrderPage = api.myOrderOrderCount(
            "provider",
            MySingleton.getInstance()?.idUser.toString()
        )

        getOrderPage?.enqueue(object : Callback<MyOrderOrder> {
            override fun onResponse(call: Call<MyOrderOrder>, response: Response<MyOrderOrder>) {
                if (response.isSuccessful) {
                    pageCount = response.body()?._page_count!!.toInt()
                    currentPage = response.body()?._page_count!!.toInt()
                    getOrderListPage(pageCount)
                }
            }

            override fun onFailure(call: Call<MyOrderOrder>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun getOrderListPage(pages: Int) {
        Log.i("lsdfjs", "page = $pages")
        getOrder = api.myOrderOrder(
            pages,
            "provider",
            MySingleton.getInstance()?.idUser.toString()
        )

        getOrder?.enqueue(object : Callback<MyOrderOrder> {
            override fun onResponse(
                call: Call<MyOrderOrder>,
                response: Response<MyOrderOrder>
            ) {
                if (response.isSuccessful) {
                    mBinding.pbMyOrderOrder.visibility = View.GONE
                    if (currentPage == pageCount)
                        recyclerAdapter.setMovieListItems(
                            response.body()?._embedded!!.orders.reversed().toMutableList()
                        )
                    else
                        recyclerAdapter.addMovieListItems(response.body()?._embedded!!.orders.reversed())


                }
            }

            override fun onFailure(call: Call<MyOrderOrder>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onItemClicked(orders: Orders) {
        val action = MyOrderFragmentDirections.actionMyOrderFragmentToOrderPaidCompletedFragment(
            orders.order_number.toString(),
            if (orders._embedded != null) orders._embedded.provider.name else "",
            orders.id,
            false
        )

        findNavController().navigate(action)
    }

    override fun onShowLastItem() {
        currentPage -= 1
        if (currentPage > 0)
            getOrderListPage(currentPage)
    }

    override fun onStop() {
        super.onStop()
        getOrder?.let { it.cancel() }
        getOrderPage?.let { it.cancel() }
    }
}