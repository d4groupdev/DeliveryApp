package com.example.app.anything

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.app.MySingleton
import com.example.app.R
import com.example.app.databinding.FragmentOrderCashBinding
import com.example.app.model.anything.anythingOrder.AnythingOrder
import com.example.app.model.anything.anythingOrder.AnythingOrderCartResponce
import com.example.app.retrofit.RetroClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class OrderCashFragment : Fragment() {

    private lateinit var mBinding: FragmentOrderCashBinding

    private val api = RetroClient.apiService
    private var dialog: Dialog? = null
    private var sendAnything: Call<AnythingOrder>? = null

    private val isSearch: Boolean? by lazy {
        arguments?.getBoolean("is_search", false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.dialog_progress_bar)

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_cash, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnCash.setOnClickListener(this::onClick)

        mBinding.etAnythingCashPhone.setText("+380")
    }

    private var isClicked = true

    private fun onClick(view: View) {
        when (view) {
            mBinding.btnCash -> {
                if (isClicked) {
                    val pattern = Pattern.compile("^\\+380\\d{9}\$")
                    val phoneString = mBinding.etAnythingCashPhone.text.toString()
                    val matcher = pattern.matcher(phoneString)
                    if (mBinding.etAnythingCashAddress.text.isNullOrEmpty()) {
                        mBinding.textFieldAnythingCashAddress.error =
                            resources.getText(R.string.sign_up_error_text)
                    } else if (mBinding.etAnythingCashHouse.text.isNullOrEmpty()
                    ) {
                        mBinding.textFieldHouse.error =
                            resources.getText(R.string.sign_up_error_text)
                        mBinding.textFieldAnythingCashAddress.error = null
                    } else if (matcher.matches()) {
                        mBinding.textFieldAnythingCashAddress.error = null
                        mBinding.textFieldHouse.error = null
                        mBinding.textFieldPhone.error = null
                        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    sendOrder()
                                    isClicked = false
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    dialog.dismiss()
                                    isClicked = true
                                }
                            }
                        }
                        val builder =
                            AlertDialog.Builder(requireContext())
                        builder.setMessage("Вы уверены?")
                            .setPositiveButton("Да", dialogClickListener)
                            .setNegativeButton("Нет", dialogClickListener).show()
                    } else {
                        mBinding.textFieldPhone.error = resources.getText(R.string.number_incorrect)
                        mBinding.textFieldAnythingCashAddress.error = null
                        mBinding.textFieldHouse.error = null
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        isClicked = true
    }

    private fun sendOrder() {
        sendAnything = api.anythingOrderCard(
            MySingleton.getInstance()?.idOffer.toString(),
            AnythingOrderCartResponce(
                payment_status = "pending",
                delivery_street_name = mBinding.etAnythingCashAddress.text.toString(),
                delivery_building_number = mBinding.etAnythingCashHouse.text.toString(),
                delivery_phone = mBinding.etAnythingCashPhone.text.toString(),
                delivery_apartment_number = mBinding.etAnythingCashFlat.text.toString(),
                payment_method = "cash",
                is_active = true
            )
        )

        sendAnything?.enqueue(object : Callback<AnythingOrder> {
            override fun onResponse(call: Call<AnythingOrder>, response: Response<AnythingOrder>) {
                val action =
                    OrderFragmentDirections.actionOrderFragmentToOrderProcessingFragment(
                        response.body()?.order_number.toString(),
                        MySingleton.getInstance()?.idOffer
                    )
                findNavController().navigate(action)
                isClicked = false
            }

            override fun onFailure(call: Call<AnythingOrder>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                isClicked = false
            }
        })
    }

    override fun onStop() {
        super.onStop()
        sendAnything?.let { it.cancel() }
    }
}
