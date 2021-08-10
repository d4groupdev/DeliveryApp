package com.example.app.restaurants.fragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.MainActivity
import com.example.app.MySingleton
import com.example.app.R
import com.example.app.databinding.FragmentResturantDeliveryBinding
import com.example.app.model.order.getOrder.OrderGet
import com.example.app.model.order.orderAll.Lines
import com.example.app.model.order.orderAll.OrderAll
import com.example.app.model.order.putOrder.Order
import com.example.app.model.order.putOrder.PutOrder
import com.example.app.model.order.putOrder.PutOrderRespons
import com.example.app.model.product.deleteProduct.DeleteProduct
import com.example.app.partners.fragment.DetailsFragment
import com.example.app.restaurants.adapter.OrderListAdapter
import com.example.app.retrofit.RetroClient
import com.example.app.utils.AppPreferences
import com.phelat.navigationresult.BundleFragment
import kotlinx.android.synthetic.main.fragment_restauranttem.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.FieldPosition
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ResturantDeliveryFragment : BundleFragment(), OrderListAdapter.Callback {
    private lateinit var mBinding: FragmentResturantDeliveryBinding

    private val model: ArrayList<Lines> = ArrayList()
    private val api = RetroClient.apiService

    lateinit var recyclerAdapter: OrderListAdapter

    var allSum: Int? = null
    private var order: Call<OrderAll>? = null
    private var deleteProduct: Call<DeleteProduct>? = null
    private var getOrder: Call<OrderGet>? = null

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_resturant_delivery,
            container,
            false
        )
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreferences = AppPreferences(requireContext())

        mBinding.toolbarRestaurantDelivery.setupWithNavController(
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
        mBinding.toolbarRestaurantDelivery.title = resources.getString(R.string.order)
        mBinding.btnRestaurantDeliveryGoToProduct.setOnClickListener(this::onClick)
        mBinding.llRestaurantDeliveryComments.setOnClickListener(this::onClick)

        recyclerAdapter =
            OrderListAdapter(model, this, object : OrderListAdapter.OrderAdapterListener {
                override fun onItemDelete(orderItem: Lines, position: Int) {
                    deleteProduct(orderItem.id!!, orderItem.embedded_order!!.item!!.id!!, position)
                }

                override fun onDeliveryClick() {
                    val action =
                        ResturantDeliveryFragmentDirections.actionResturantDeliveryFragmentToRestaurantDeliveryTermsFragment()
                    findNavController().navigate(action)
                }
            })

        mBinding.rwRestaurantDelivery.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = recyclerAdapter
        }

        if (!MySingleton.getInstance()?.comments.isNullOrEmpty()) {
            mBinding.tvRestaurantDetails.text = MySingleton.getInstance()?.comments
        } else {
            mBinding.tvRestaurantDetails.text = resources.getText(R.string.comments_delivery)
        }

        (requireActivity() as MainActivity).onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                view.post { findNavController().popBackStack() }
            }
        })

        order = api.orderLine(appPreferences.getString("ID_ORDER").toString())

        order?.enqueue(object : Callback<OrderAll> {
            override fun onResponse(call: Call<OrderAll>, response: Response<OrderAll>) {

                if (response.isSuccessful) {
                    mBinding.lldelivery.visibility = View.VISIBLE
                    mBinding.pbDelivery.visibility = View.GONE
                    recyclerAdapter.setMovieListItems(response.body()?.embedded_line?.lines!!.toMutableList())
                }
            }

            override fun onFailure(call: Call<OrderAll>, t: Throwable) {
                context?.let {
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        val deliveryData = appPreferences.getDeliverData()
        if (!deliveryData.delivery_details.isNullOrBlank() && deliveryData.delivery_details != "Недоставляем") {
            recyclerAdapter.setDeliverPrice(deliveryData.delivery_details!!.toInt())
        }
    }

    private fun deleteProduct(id: String?, idProduct: String, position: Int) {
        deleteProduct = api.deleteProduct(id.toString())
        deleteProduct?.enqueue(object : Callback<DeleteProduct> {
            override fun onResponse(call: Call<DeleteProduct>, response: Response<DeleteProduct>) {
                appPreferences.putString(idProduct, "")
                recyclerAdapter.deleteItem(position)
                updateCost()
            }

            override fun onFailure(call: Call<DeleteProduct>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        isClicked = true
        updateCost()
    }

    fun updateCost() {
        getOrder = api.getOrder(appPreferences.getString("ID_ORDER"))
        getOrder?.enqueue(object : Callback<OrderGet> {
            override fun onResponse(call: Call<OrderGet>, response: Response<OrderGet>) {
                if (response.body() != null && response.body()?.quantity!! > 0) {
                    val sumDelivery = response.body()?.delivery_cost

                    mBinding.tvOrderAllSum.text = response.body()?.value!!.toInt().toString()

                    allSum = response.body()?.value!!.toInt()
                } else {
                    findNavController().popBackStack()
                }
            }

            override fun onFailure(call: Call<OrderGet>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var isClicked = true

    private fun onClick(view: View) {
        when (view) {

            mBinding.btnRestaurantDeliveryGoToProduct -> {

                if (isClicked) {

                    val currentTime = Calendar.getInstance()
                    val openTime = appPreferences.getRestaurantOpenTime()
                    val closeTime = appPreferences.getRestaurantCloseTime()
                    var timeOpen = openTime!!.get(Calendar.HOUR_OF_DAY)
                    var timeClose =
                        if (closeTime!!.get(Calendar.HOUR_OF_DAY) < timeOpen) closeTime!!.get(
                            Calendar.HOUR_OF_DAY
                        ) + 24 else closeTime!!.get(Calendar.HOUR_OF_DAY)
                    if (currentTime.get(Calendar.HOUR_OF_DAY) > openTime.get(Calendar.HOUR_OF_DAY) &&
                        currentTime.get(Calendar.HOUR_OF_DAY) < timeClose
                    ) {
                        goToNextScreen()
                    } else if (currentTime.get(Calendar.HOUR_OF_DAY) == openTime.get(Calendar.HOUR_OF_DAY) &&
                        currentTime.get(Calendar.MINUTE) > openTime.get(Calendar.MINUTE)
                    ) {
                        goToNextScreen()
                    } else if (currentTime.get(Calendar.HOUR_OF_DAY) == timeClose &&
                        currentTime.get(Calendar.MINUTE) < closeTime!!.get(Calendar.MINUTE)
                    ) {
                        goToNextScreen()
                    } else {
                        context?.let {
                            Toast.makeText(
                                it, String.format(
                                    getString(R.string.restaurant_closed),
                                    openTime.get(Calendar.HOUR_OF_DAY),
                                    openTime.get(Calendar.MINUTE),
                                    closeTime.get(Calendar.HOUR_OF_DAY),
                                    closeTime.get(Calendar.MINUTE)
                                ), Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    isClicked = false
                }
            }

            mBinding.llRestaurantDeliveryComments -> {
                navigate(
                    ResturantDeliveryFragmentDirections.actionResturantDeliveryFragmentToDetailsFragment(
                        "restaurant"
                    ), REQUEST_CODE
                )
            }
        }
    }

    fun goToNextScreen() {
        val action =
            ResturantDeliveryFragmentDirections.actionResturantDeliveryFragmentToRestaurantOrderFragment(
                "",
                allSum.toString(),
                MySingleton.getInstance()?.comments.toString()
            )
        findNavController().navigate(action)
    }

    override fun onFragmentResult(requestCode: Int, bundle: Bundle) {
        if (requestCode == REQUEST_CODE) {
            val isLoginSuccessful = bundle.getString(DetailsFragment.CATEGORY_NAME, "")
        }
    }

    override fun onItemClicked(item: Lines) {
        Log.d("sadalasdsadasd", item.embedded_order?.item?.id.toString())
        val action =
            ResturantDeliveryFragmentDirections.actionResturantDeliveryFragmentToRestaurantProductFragment(
                item.embedded_order?.item?.id
            )
        action.isOrder = "delivery"
        findNavController().navigate(action)
    }

    override fun delivery(item: Lines) {
        val action =
            ResturantDeliveryFragmentDirections.actionResturantDeliveryFragmentToRestaurantDeliveryTermsFragment()
        findNavController().navigate(action)
    }

    companion object {
        const val REQUEST_CODE = 123
    }

    override fun onStop() {
        super.onStop()
        order?.let { it.cancel() }
        deleteProduct?.let { it.cancel() }
        getOrder?.let { it.cancel() }
    }
}
