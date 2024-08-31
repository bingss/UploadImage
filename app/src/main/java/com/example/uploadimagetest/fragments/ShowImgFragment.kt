package com.example.uploadimagetest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uploadimagetest.R
import com.example.uploadimagetest.databinding.FragmentMapBinding
import com.example.uploadimagetest.databinding.FragmentShowImgBinding
import com.example.uploadimagetest.util.MapManager
import com.example.uploadimagetest.util.showLog
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch



/**
 * A simple [Fragment] subclass.
 * Use the [ShowImgFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShowImgFragment : Fragment() {

    private var _binding: FragmentShowImgBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowImgBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}