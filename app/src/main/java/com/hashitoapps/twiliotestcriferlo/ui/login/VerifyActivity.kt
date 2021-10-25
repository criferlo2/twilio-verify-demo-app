package com.hashitoapps.twiliotestcriferlo.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.hashitoapps.twiliotestcriferlo.R
import com.hashitoapps.twiliotestcriferlo.databinding.ActivityVerifyBinding
import com.hashitoapps.twiliotestcriferlo.ui.login.viewmodel.LoginViewModel
import com.hashitoapps.twiliotestcriferlo.ui.login.viewmodel.LoginViewModelFactory
import com.hashitoapps.twiliotestcriferlo.ui.login.viewmodel.PendingResult
import com.twilio.verify.models.ChallengeStatus

class VerifyActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    var factorSid = ""
    var challengeSid = ""
    var message = ""

    private lateinit var binding: ActivityVerifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        binding = ActivityVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(application))
            .get(LoginViewModel::class.java)

        factorSid = intent.extras?.get("factorSid").toString()
        challengeSid = intent.extras?.get("challengeSid").toString()
        message = intent.extras?.get("message").toString()

        getPending(factorSid, challengeSid , message)
        initButtons()
    }

    private fun initButtons() {
        binding.accept.setOnClickListener {
            loginViewModel.updateChallenge(factorSid,challengeSid,ChallengeStatus.Approved)
            loginViewModel.updateChallengeLiveDataNM.observe(this, { result->
                if(result is PendingResult.PendingSuccess){
                    loginViewModel.clearStorage(factorSid)
                    binding.accept.isEnabled = false
                    binding.deny.isEnabled = false
                    Toast.makeText(this,result.value,Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "Error in request.", Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.deny.setOnClickListener {
            loginViewModel.updateChallenge(factorSid,challengeSid,ChallengeStatus.Denied)
            loginViewModel.updateChallengeLiveDataNM.observe(this, { result->
                if(result is PendingResult.PendingSuccess){
                    binding.accept.isEnabled = false
                    binding.deny.isEnabled = false
                    Toast.makeText(this,result.value,Toast.LENGTH_LONG).show()
                }else{
                    binding.accept.isEnabled = false
                    binding.deny.isEnabled = false
                    Toast.makeText(this, "Error in request.", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun getPending(factorSid_: String, challengeSid_: String , message:String) {
        loginViewModel.getPending(factorSid_,challengeSid_)
        loginViewModel.pendingLiveDataNM.observe(this, { result->
           if(result is PendingResult.PendingSuccess){
               result.value
           }else{
               binding.accept.isEnabled = false
               binding.deny.isEnabled = false
               Toast.makeText(this,"We have troubles, please try uninstalling app and try again",Toast.LENGTH_LONG).show()
           }
        })
    }




}