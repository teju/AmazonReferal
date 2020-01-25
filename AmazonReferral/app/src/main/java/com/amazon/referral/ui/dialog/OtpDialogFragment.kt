package com.memu.ui.dialog

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.referral.R
import com.amazon.referral.libs.BaseHelper
import com.iapps.gon.etc.callback.NotifyListener
import kotlinx.android.synthetic.main.otp_dialog.*

class OtpDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.otp_dialog

    companion object {
        val TAG = "OtpDialogFragment"
    }

    lateinit var listener: NotifyListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_positive.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                listener.let {
                    if(!BaseHelper.isEmpty(otp.text.toString())) {
                        error.visibility = View.GONE
                        listener.onButtonClicked(otp.text.toString().toInt())
                    } else {
                        error.visibility = View.VISIBLE
                    }
                }
                dismiss()
            }
        })

    }

}