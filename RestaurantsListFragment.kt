package com.example.app.restaurants.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.MainActivity
import com.example.app.MySingleton
import com.example.app.R
import com.example.app.databinding.FragmentRestaurantsListBinding
import com.example.app.model.categorieRestaurant.ProviderCategorie
import com.example.app.model.categorieRestaurant.ProviderTags
import com.example.app.model.restaurant.Providers
import com.example.app.model.restaurant.Restaurant
import com.example.app.restaurants.adapter.MenuAllRestaurantAdapter
import com.example.app.restaurants.adapter.RecyclerCategorieTagsAdapter
import com.example.app.retrofit.MyInterface
import com.example.app.retrofit.RetroClient
import com.example.app.utils.AppPreferences
import com.example.app.utils.CommonFragmentPagerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RestaurantsListFragment : Fragment(), RecyclerCategorieTagsAdapter.Callback,
    MenuAllRestaurantAdapter.Callback {

    private lateinit var mBinding: FragmentRestaurantsListBinding

    private lateinit var fragmentPagerAdapter: CommonFragmentPagerAdapter
    private val api = RetroClient.apiService
    private var array: Array<String>? = null
    lateinit var recyclerAdapter: RecyclerCategorieTagsAdapter
    private val model: ArrayList<ProviderTags> = ArrayList()

    private val modelTab: MutableList<Providers> = ArrayList()
    lateinit var recyclerAdapterTab: MenuAllRestaurantAdapter

    private var pageCount = 1
    private var currentPage = 1
    private var myInterface: Call<Restaurant>? = null
    private var providerCateg: Call<ProviderCategorie>? = null
    private var myInterfaceList: Call<Restaurant>? = null

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_restaurants_list, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.toolbarRestaurantList.setupWithNavController(
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
        mBinding.toolbarRestaurantList.title = resources.getString(R.string.restaurant)
        mBinding.toolbarRestaurantList.setNavigationIcon(R.drawable.ic_arrow_new_left)
        mBinding.toolbarRestaurantList.setNavigationOnClickListener(View.OnClickListener {
            view.post {
                val action =
                    RestaurantsListFragmentDirections.actionRestaurantsListFragmentToMainFragment()
                findNavController().navigate(action)
            }
        })

        (requireActivity() as MainActivity).onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                view.post {
                    val action =
                        RestaurantsListFragmentDirections.actionRestaurantsListFragmentToMainFragment()
                    findNavController().navigate(action)
                }
            }
        })

        appPreferences = AppPreferences(requireContext())


        if (appPreferences.getString("ID_RESTAURANT").isNotEmpty()) {
            val action =
                RestaurantsListFragmentDirections.actionRestaurantsListFragmentToRestauranttemFragment(
                    appPreferences.getString("ID_RESTAURANT"),
                    appPreferences.getString("NAME_RESTAURANT")
                )
            MySingleton.getInstance()?.idRestaurant = appPreferences.getString("NAME_RESTAURANT")
            findNavController().navigate(action)
        }

        recyclerAdapter = RecyclerCategorieTagsAdapter(model, this)

        mBinding.rwCategorieTags.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recyclerAdapter
        }

        recyclerAdapterTab = MenuAllRestaurantAdapter(modelTab, this)

        mBinding.rwRestaurantMenu.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = recyclerAdapterTab
        }

        getRestaurantList(1)

        providerCateg = api.restaurantCategory()
        providerCateg?.enqueue(object : Callback<ProviderCategorie> {
            override fun onResponse(
                call: Call<ProviderCategorie>, response: Response<ProviderCategorie>
            ) {
                recyclerAdapter.setMovieListItems(response.body()?._embedded?.provider_tags!!)
            }

            override fun onFailure(call: Call<ProviderCategorie>, t: Throwable) {
            }
        })
    }

    override fun onItemClickedTag(providerTags: ProviderTags) {
        recyclerAdapterTab.clearList()
        if (providerTags.id == null) {
            getRestaurantList(1)
        } else {
            getRestaurantCategoryList(providerTags.id)
        }
    }

    private fun getRestaurantList(page: Int) {
        myInterfaceList = api.restaurantList(page, "position")
        myInterfaceList?.enqueue(object : Callback<Restaurant> {
            override fun onResponse(call: Call<Restaurant>, response: Response<Restaurant>) {
                if (response.isSuccessful) {
                    if (mBinding.pbAllRestaurant != null) {
                        mBinding.pbAllRestaurant.visibility = View.GONE
                        recyclerAdapterTab.setMovieListItems(response.body()?._embedded?.providers!!)
                        currentPage = response.body()!!._page
                        pageCount = response.body()!!._page_count
                    }
                }
            }

            override fun onFailure(call: Call<Restaurant>, t: Throwable) {
                Log.d("MYTGAAMKAKKA", "call " + t.toString())
            }
        })
    }

    private fun getRestaurantCategoryList(tagId: String?) {
        myInterface = api.getRestorauntCategory(tagId.toString(), "position")!!
        myInterface?.enqueue(object : Callback<Restaurant> {
            override fun onResponse(call: Call<Restaurant>, response: Response<Restaurant>) {
                recyclerAdapterTab.setMovieListItems(response.body()?._embedded?.providers!!)
            }

            override fun onFailure(call: Call<Restaurant>, t: Throwable) {
                Log.d("MYTGAAMKAKKA", "call " + t.toString())
            }
        })
    }

    override fun onItemClicked(item: Providers) {
        val action =
            RestaurantsListFragmentDirections.actionRestaurantsListFragmentToRestauranttemFragment(
                item.id,
                item.name
            )
        appPreferences.putString("ID_RESTAURANT", item.id)
        appPreferences.putString("NAME_RESTAURANT", item.name)
        MySingleton.getInstance()?.idRestaurant = item.id
        findNavController().navigate(action)
    }

    override fun onShowLastItemRestaurant() {
        if (currentPage <= pageCount)
            getRestaurantList(currentPage + 1)
    }


    override fun onStop() {
        super.onStop()
        myInterface?.let { it.cancel() }
        providerCateg?.let { it.cancel() }
        myInterfaceList?.let { it.cancel() }
    }
}
