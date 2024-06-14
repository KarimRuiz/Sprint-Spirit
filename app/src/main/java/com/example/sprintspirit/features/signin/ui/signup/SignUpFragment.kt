package com.example.sprintspirit.features.signin.ui.signup

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.text.HtmlCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.adapters.TextViewBindingAdapter
import androidx.lifecycle.ViewModelProvider
import com.example.sprintspirit.R
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.databinding.FragmentSignUpBinding
import com.example.sprintspirit.features.signin.ui.signin.SignInActivity
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils


class SignUpFragment : BaseFragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var dbManager: DBManager
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbManager = DBManager.getCurrentDBManager()
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        navigator = SprintSpiritNavigator(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentSignUpBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentSignUpBinding) {
        binding.edtWeight.doAfterTextChanged {s->
            if (!s.isNullOrBlank() && s.toString().toDouble() > viewModel.maxWeight) {
                binding.edtWeight.error = getString(R.string.Weight_low_limit) + viewModel.maxWeight
            }
            if (!s.isNullOrBlank() && s.toString().toDouble() < viewModel.minWeight) {
                binding.edtWeight.error = getString(R.string.Weight_high_limit) + viewModel.minWeight
            }
        }

        binding.edtHeight.doAfterTextChanged {s->
            if (!s.isNullOrBlank() && s.toString().toDouble() > viewModel.maxHeight) {
                binding.edtHeight.error = getString(R.string.Height_low_limit) + viewModel.maxHeight
            }
            if (!s.isNullOrBlank() && s.toString().toDouble() < viewModel.minHeight) {
                binding.edtHeight.error = getString(R.string.Height_high_limit) + viewModel.minHeight
            }
        }

        binding.edtPassword.doAfterTextChanged {
            if((!it.isNullOrBlank()) and (it.toString().length < 7)){
                binding.edtPassword.error = getString(R.string.Password_min_characters)
            }
        }

        binding.edtConfirmPassword.doAfterTextChanged {
            val password = binding.edtPassword.text.toString()
            val confirmPassword = it.toString()
            if(confirmPassword.length > 6 && password != confirmPassword){
                binding.edtConfirmPassword.error = getString(R.string.Passwords_do_not_match)
            }
        }

        binding.edtEmail.doAfterTextChanged {
            if(!it.isNullOrBlank() and !Utils.isValidEmail(it!!)){
                binding.edtEmail.error = getString(R.string.Email_not_valid)
            }
        }

        binding.edtUsername.doAfterTextChanged {
            if((!it.isNullOrBlank()) and (it.toString().length < 5)){
                binding.edtUsername.error = getString(R.string.Username_min_length)
            }
        }

        binding.cbTerms.setOnCheckedChangeListener { buttonView, isChecked ->
            checkAllDataIsFilled()
        }

        binding.termsAndConditions = View.OnClickListener {
            OpenTermsAndConditions()
        }

        binding.writeText = TextViewBindingAdapter.AfterTextChanged{
            checkAllDataIsFilled()
        }

        binding.goToLogIn = View.OnClickListener {
            val logInIntent = Intent(activity, SignInActivity::class.java)
            startActivity(logInIntent)
        }

        binding.signUpWithEmail = View.OnClickListener {
            dbManager.signUpWithEmail(
                binding.edtUsername.text.toString(),
                binding.edtEmail.text.toString(),
                binding.edtPassword.text.toString(),
                binding.edtWeight.text.toString().toDouble(),
                binding.edtHeight.text.toString().toDouble(),
                {
                    dbManager.logInWithEmail(
                        binding.edtEmail.text.toString(),
                        binding.edtPassword.text.toString(),
                        {
                            sharedPreferences.email = binding.edtEmail.text.toString()
                            showHome()
                        },
                        { context?.let { it1 -> showAlert(it1.getString(R.string.Sign_in_error)) } }
                    )
                },
                { context?.let { it1 -> showAlert(it1.getString(R.string.Sign_up_error)) } }
            )
        }
    }

    private fun checkAllDataIsFilled() {
        val binding = binding as FragmentSignUpBinding
        binding.btLogin.isEnabled =
            (binding.edtPassword.text.length >= 6) and
                    (binding.edtUsername.text.length >= 4) and
                    Utils.isValidEmail(binding.edtEmail.text) and
                    weightInLimit(binding) and
                    heightInLimit(binding) and
                    (binding.cbTerms.isChecked) and
                    (binding.edtPassword.text.toString() == binding.edtConfirmPassword.text.toString())
    }


    private fun OpenTermsAndConditions() {
        (binding as FragmentSignUpBinding).termsAndConditionsWindow.visibility = View.VISIBLE
        val termsText = requireContext().getString(R.string.terms_complete_text)
        (binding as FragmentSignUpBinding).termsPopUpText.text =
            HtmlCompat.fromHtml(termsText, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()

        (binding as FragmentSignUpBinding).termsAndConditionsCloseWindow.setOnClickListener {
            (binding as FragmentSignUpBinding).termsAndConditionsWindow.visibility = View.GONE
        }
    }

    private fun heightInLimit(binding: FragmentSignUpBinding): Boolean {
        val heightText = binding.edtHeight.text.toString()
        val height = heightText.toDoubleOrNull() ?: return false
        return height in viewModel.minHeight..viewModel.maxHeight
    }

    private fun weightInLimit(binding: FragmentSignUpBinding): Boolean {
        val weightText = binding.edtWeight.text.toString()
        val weight = weightText.toDoubleOrNull() ?: return false
        return weight in viewModel.minWeight..viewModel.maxWeight
    }

    private fun showHome() {
        navigator.navigateToHome(activity, true)
    }

}