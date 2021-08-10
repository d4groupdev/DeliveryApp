package com.example.app.partners.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.app.MainActivity
import com.example.app.MySingleton

import com.example.app.R
import com.example.app.databinding.FragmentPartnersDeliveryDetailsBinding
import com.example.app.partners.fragment.DetailsFragmentArgs
import com.example.app.restaurants.fragment.ResturantDeliveryFragment
import com.phelat.navigationresult.navigateUp

class DetailsFragment : Fragment() {

    private lateinit var mBinding: FragmentPartnersDeliveryDetailsBinding

    var isDetails: String? = null
    private val safeArgs: DetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_partners_delivery_details, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isDetails = safeArgs.isDetails.toString()
        mBinding.btnDetailsAccept.setOnClickListener(this::onClick)

        when (isDetails){
            "partner" -> {
                mBinding.toolbarDetails.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))
                mBinding.toolbarDetails.title = resources.getString(R.string.details)
                mBinding.textFieldEmail.hint = resources.getString(R.string.patners_details_delivery)
            }

            "restaurant" -> {
                mBinding.toolbarDetails.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))
                mBinding.toolbarDetails.title = resources.getString(R.string.comments)
                mBinding.textFieldEmail.hint = resources.getString(R.string.comments_order)
            }
        }
    }


    private fun onClick(view: View) {
        when(view) {
            mBinding.btnDetailsAccept -> {
                val bundle = Bundle().apply {
                    putString(CATEGORY_NAME, mBinding.etDetails.text.toString())
                    MySingleton.getInstance()?.comments = mBinding.etDetails.text.toString()

                    if (requireActivity() is MainActivity) {
                        (requireActivity() as MainActivity).hideKeyboard()
                    }
                }

                when(isDetails) {
                    "partner" -> {}

                    "restaurant" -> {
                        navigateUp(
                            ResturantDeliveryFragment.REQUEST_CODE,
                            bundle)
                    }
                }
            }
        }
    }

    companion object {
        const val CATEGORY_NAME = "details_name"
    }
}
