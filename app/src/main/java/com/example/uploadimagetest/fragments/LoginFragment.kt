package com.example.uploadimagetest.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uploadimagetest.R
import com.example.uploadimagetest.data.Tname
import com.example.uploadimagetest.data.TownData
import com.example.uploadimagetest.data.model.UiState
import com.example.uploadimagetest.databinding.FragmentLoginBinding
import com.example.uploadimagetest.util.EncryptedSharedHelper
import com.example.uploadimagetest.util.getJson
import com.example.uploadimagetest.util.showLog
import com.example.uploadimagetest.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val vm : LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var encryptedShared: EncryptedSharedHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        encryptedShared = EncryptedSharedHelper(requireActivity(),"Encrypted_Data")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(encryptedShared.getBoolean("remember"))
        {
            binding.accountEditText.setText(encryptedShared.getString("account"))
            binding.passwordEditText.setText(encryptedShared.getString("password"))
            binding.rememberCheckBox.isChecked = true
        }

        binding.loginButton.setOnClickListener {
            val account = binding.accountEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            lifecycleScope.launch {
                vm.login(account,password,binding.rememberCheckBox.isChecked,encryptedShared)
            }
        }

        vm.currentState.observe(viewLifecycleOwner){
            when (it) {
                UiState.LOADING -> {
                    binding.loginButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                UiState.IDLE -> {
                    binding.loginButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireActivity(), "${vm.userinfo.機關},${vm.userinfo.姓名}登入成功!", Toast.LENGTH_SHORT).show()

                    val bundle = bundleOf("tname_chinese" to vm.userinfo.機關)
                    findNavController().navigate(R.id.action_loginFragment_to_caseFragment,bundle)
                }
                UiState.ERROR -> {
                    binding.loginButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireActivity(), "帳號密碼錯誤", Toast.LENGTH_SHORT).show()
                }

                UiState.INIT -> {  }
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}