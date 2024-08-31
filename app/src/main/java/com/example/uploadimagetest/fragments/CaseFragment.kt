package com.example.uploadimagetest.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uploadimagetest.R
import com.example.uploadimagetest.data.model.UiState
import com.example.uploadimagetest.databinding.FragmentCaseBinding
import com.example.uploadimagetest.util.showLog
import com.example.uploadimagetest.viewmodel.CaseViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.LocalDate

class CaseFragment : Fragment() {

    private val vm : CaseViewModel by viewModels()
    private var _binding: FragmentCaseBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = CaseFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginBundle = arguments?.getString("tname_chinese")

        binding.searchButton.setOnClickListener {
            lifecycleScope.launch {
                if(loginBundle == "地政局"){ binding.tnameTextView.text = binding.officeSpinner.selectedItem.toString() }

                vm.getCase(binding.tnameTextView.text.toString(),
                    binding.yearEditText.getText().toString(),
                    binding.caseidEditText.getText().toString(),
                    requireActivity() )
            }
        }

        vm.currentState.observe(viewLifecycleOwner){
            when (it) {
                UiState.LOADING -> {
                    binding.searchButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                UiState.IDLE -> {
                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(requireActivity(), "${vm.BSWebinfo.town}---${vm.BSWebinfo.sectno}---${vm.BSWebinfo.landno}", Toast.LENGTH_SHORT).show()
                    val bundle = bundleOf("BSWebinfo" to Gson().toJson(vm.BSWebinfo) )
                    findNavController().navigate(R.id.action_caseFragment_to_mapFragment,bundle )
                }
                UiState.ERROR -> {
                    binding.searchButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireActivity(), "查無資料", Toast.LENGTH_SHORT).show()
                }

                UiState.INIT -> {
                    //設定初始年度
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        binding.yearEditText.setText( (LocalDate.now().year-1911).toString() )
                    }
                    //地政局設定spinner
                    if(loginBundle == "地政局"){
                        val adapter = ArrayAdapter.createFromResource(
                            requireActivity(),
                            R.array.office,
                            android.R.layout.simple_spinner_dropdown_item)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.officeSpinner.setAdapter(adapter)
                        binding.officeSpinner.setSelection(0)
                        binding.officeSpinner.visibility=View.VISIBLE
                        binding.tnameTextView.visibility = View.GONE
                    }
                    else{ //地所->文字
                        binding.tnameTextView.text = loginBundle
                        binding.officeSpinner.visibility=View.GONE
                        binding.tnameTextView.visibility=View.VISIBLE
                    }
                }
            }
        }

    }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding = null
    }

}