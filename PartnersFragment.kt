package com.example.app.partners.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.app.MySingleton
import com.example.app.R
import com.example.app.databinding.FragmentPartnersBinding
import com.example.app.model.login.Login
import com.example.app.retrofit.RetroClient
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartnersFragment : Fragment() {

    private lateinit var mBinding: FragmentPartnersBinding

    private val api = RetroClient.apiService
    private var loginPartners: Call<Login>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_partners, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.apply {
            btnLogin.setOnClickListener(this@PartnersFragment::onClick)
            toolbarPartners.setupWithNavController(
                findNavController(),
                AppBarConfiguration(findNavController().graph)
            )
        }
    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.btnLogin -> {
                val basic = Credentials.basic(
                    mBinding.etPartnerEmail.text.toString(),
                    mBinding.etPartnerPassword.text.toString()
                )
                loginPartners = api.login(basic)
                loginPartners?.enqueue(object : Callback<Login> {
                    override fun onResponse(call: Call<Login>, response: Response<Login>) {
                     
                        MySingleton.getInstance()?.idPartners = response.body()?.token

                        if (response.isSuccessful) {
                            val action =
                                PartnersFragmentDirections.actionPartnersFragmentToPatnerListFragment()
                            findNavController().navigate(action)
                        }
                    }

                    override fun onFailure(call: Call<Login>, t: Throwable) {}
                })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        loginPartners?.let { it.cancel() }
    }
}
