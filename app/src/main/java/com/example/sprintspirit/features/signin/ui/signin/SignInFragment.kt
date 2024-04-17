package com.example.sprintspirit.features.signin.ui.signin

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.adapters.TextViewBindingAdapter
import com.example.sprintspirit.R
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.databinding.FragmentSignInBinding
import com.example.sprintspirit.features.signin.ui.signup.SignUpActivity
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.example.sprintspirit.util.Utils

class SignInFragment : BaseFragment() {

    companion object {
        fun newInstance() = SignInFragment()
    }

    private lateinit var dbManager: DBManager
    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbManager = DBManager.getCurrentDBManager()
        viewModel = ViewModelProvider(this).get(SignInViewModel::class.java)
        navigator = SprintSpiritNavigator(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentSignInBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentSignInBinding) {
        binding.logInWithEmail = View.OnClickListener{
            dbManager.logInWithEmail(
                binding.edtEmail.text.toString(),
                binding.edtPassword.text.toString(),
                { sharedPreferences.email = binding.edtEmail.text.toString()
                    showHome()
                },
                { context?.let { it1 -> showAlert(it1.getString(R.string.Sign_in_error)) } }
            )
        }

        binding.writeText = TextViewBindingAdapter.AfterTextChanged{
            binding.btLogin.isEnabled =
                (binding.edtPassword.text.length >= 6) and Utils.isValidEmail(binding.edtEmail.text)
        }

        binding.goToSignUp = View.OnClickListener {
            val signUpIntent = Intent(activity, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
    }

    private fun showHome() {
        navigator.navigateToHome(activity, true)
    }

    /*
    PARA EL REGISTRO:
    FirebaseAuth.getInstance().createUserWithEmailAndPassword()
    https://youtu.be/dpURgJ4HkMk?si=cHTHJK5nGWUQzet4&t=668
     */

}