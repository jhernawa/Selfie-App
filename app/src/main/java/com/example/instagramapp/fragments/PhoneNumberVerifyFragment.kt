package com.example.instagramapp.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast

import com.example.instagramapp.R
import com.example.instagramapp.activities.MainActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_phone_number_verify.view.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PhoneNumberVerifyFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PhoneNumberVerifyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val KEY_MOBILE = "mobile"

class PhoneNumberVerifyFragment : Fragment() {

    var verificationId: String? = null

    lateinit var auth: FirebaseAuth

    private var mobile: String? = null

    var editText: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mobile = it.getString(KEY_MOBILE)
        }

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_phone_number_verify, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        editText = view.edit_text_code

        sendVerificationCode(mobile!!)

        view.button_verify.setOnClickListener {
            //The user manually enters the code
            var code = view.edit_text_code.text.toString()
            sendVerificationCode(code)
        }
    }

    private fun sendVerificationCode(mobile : String){
        Log.i("send", mobile)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+1$mobile",
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallBack
        )
    }



    var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            //automatically get the code from the sms that we sent and put it
            //back to the edit text. Then, we verify the code
            var code =  phoneAuthCredential.smsCode
            Log.i("send", code.toString())
            if(code != null){
                editText?.setText(code)
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()

        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            verificationId = p0

        }

    }

    private fun verifyCode(code: String){
        var credential = PhoneAuthProvider.getCredential(verificationId!!, code)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!, object: OnCompleteListener<AuthResult> {
                override fun onComplete(task: Task<AuthResult>) {
                    if(task.isSuccessful){
                        Toast.makeText(activity, "Login is successful", Toast.LENGTH_SHORT).show()
                        activity!!.startActivity(Intent(activity, MainActivity::class.java))
                    }else{
                        Toast.makeText(activity, "Login is failed", Toast.LENGTH_SHORT).show()
                    }
                }

            })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
            PhoneNumberVerifyFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_MOBILE, param1)

                }
            }
    }
}
