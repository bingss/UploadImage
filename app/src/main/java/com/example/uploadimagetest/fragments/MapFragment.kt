package com.example.uploadimagetest.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uploadimagetest.BuildConfig
import com.example.uploadimagetest.R
import com.example.uploadimagetest.data.BSWebLandData
import com.example.uploadimagetest.data.CameraPhoto
import com.example.uploadimagetest.data.model.UiState
import com.example.uploadimagetest.databinding.FragmentMapBinding
import com.example.uploadimagetest.fragments.adapter.ImgAdapter
import com.example.uploadimagetest.fragments.adapter.PointAdapter
import com.example.uploadimagetest.listener.PointClickListener
import com.example.uploadimagetest.util.MapManager
import com.example.uploadimagetest.util.deleteFile
import com.example.uploadimagetest.util.getPhotoFileUri
import com.example.uploadimagetest.util.showLog
import com.example.uploadimagetest.viewmodel.MapViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.gson.Gson
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment(), PointClickListener {


    private val vm : MapViewModel by viewModels()
    private lateinit var mapManager : MapManager
    private var _binding: FragmentMapBinding? = null
    private lateinit var photoData: CameraPhoto
    private var mapReady = false


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(requireActivity())
                    .setTitle("回到案件查詢?")
                    .setMessage("目前編輯資料將遺失")
                    .setPositiveButton("確定") { _, _ ->
                        findNavController().popBackStack()
                    }
                    .setNeutralButton("取消") { _, _ ->  }
                    .show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            binding.mapView.onCreate(savedInstanceState)
            mapManager = MapManager( binding.mapView.awaitMap() )
            mapReady = true
            mapLoaded()
        }
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = Gson().fromJson(arguments?.getString("BSWebinfo"), BSWebLandData::class.java)
        lifecycleScope.launch {
            vm.getLandInfo(bundle)
        }

        binding.btnPhoto.setOnClickListener {
            if( binding.tvBottom.text.isNotEmpty() ) imagePickerLauncher.launch("image/*")
            else Toast.makeText(requireActivity(), "請選擇或新增點號", Toast.LENGTH_SHORT).show()
        }

        binding.btnCamera.setOnClickListener {
            if( binding.tvBottom.text.isNotEmpty() ){
                try {
                    photoData = getPhotoFileUri(requireActivity())
                    cameraLauncher.launch( photoData.fileUri )
                }
                catch (e: Exception) {
                    showLog("捕獲錯誤:$e")
                }
            }
            else Toast.makeText(requireActivity(), "請選擇或新增點號", Toast.LENGTH_SHORT).show()
        }

        binding.uploadFab.setOnClickListener {
            lifecycleScope.launch {
                vm.uploadImg(requireActivity(),bundle) { progress ->
                    requireActivity().runOnUiThread {
                        binding.progressTextView.text = "上傳中...$progress%"
                    }
                }
                binding.progressTextView.text = "處理中..."
            }
        }

        binding.addFab.setOnClickListener {
            binding.tvBottom.text = vm.addPoint()
            binding.pointRecycleView.adapter?.notifyItemInserted(vm.pointList.size-1)
            Toast.makeText(requireActivity(), "新增點號${binding.tvBottom.text}", Toast.LENGTH_SHORT).show()
        }

        vm.info.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), vm.info.value, Toast.LENGTH_SHORT).show()
            if(vm.info.value == "上傳成功!"){
                lifecycleScope.launch {
                    deleteFile("${requireActivity().filesDir.path}/img")
                    val bundleToCase = bundleOf("tname_chinese" to bundle.機關)
                    findNavController().navigate(R.id.action_mapFragment_to_caseFragment,bundleToCase)
                }
            }
        }

        vm.isMapMode.observe(viewLifecycleOwner){
            when (it) {
                true -> {
                    binding.mapView.visibility = View.VISIBLE
                    binding.pointRecycleView.visibility = View.GONE
                    binding.addFab.visibility = View.GONE
                }
                false -> {
                    binding.mapView.visibility = View.GONE
                    binding.pointRecycleView.visibility = View.VISIBLE
                    binding.addFab.visibility = View.VISIBLE
                }
            }
        }

        binding.switchFab.setOnClickListener {
            vm.changeMapMode()
            BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_COLLAPSED
        }

        vm.currentState.observe(viewLifecycleOwner){ uiState ->
            when (uiState) {
                UiState.LOADING -> {
                    binding.switchFab.isEnabled = false
                    binding.mapView.isClickable = false
                    binding.addFab.visibility = View.GONE
                    binding.pointRecycleView.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressTextView.visibility = View.VISIBLE
                }
                UiState.IDLE -> {
                    binding.switchFab.isEnabled = true
                    binding.mapView.isClickable = true
                    binding.progressBar.visibility = View.GONE
                    binding.progressTextView.visibility = View.GONE

                    //地圖,recycleview,listener初始化
                    binding.pointRecycleView.layoutManager = LinearLayoutManager(context)
                    binding.pointRecycleView.adapter = PointAdapter(vm.pointList,this)
                    binding.imgRecycleView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
                    mapLoaded()
                }
                UiState.ERROR -> {
                    binding.pointRecycleView.layoutManager = LinearLayoutManager(context)
                    binding.pointRecycleView.adapter = PointAdapter(vm.pointList,this)
                    binding.imgRecycleView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
                    binding.pointRecycleView.visibility = View.VISIBLE
                    binding.addFab.visibility = View.VISIBLE
                    binding.switchFab.visibility = View.GONE
                    binding.mapView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.progressTextView.visibility = View.GONE
                    BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_COLLAPSED
                }

                UiState.INIT -> {  }

            }
        }

    }



    override fun onDeleteButtonClick(pointNum: String, position: Int) {
        val range = vm.deletePoint(position)
        mapManager.removePoint(pointNum)
        binding.pointRecycleView.adapter?.notifyItemRemoved(position)
        binding.pointRecycleView.adapter?.notifyItemRangeChanged(position,range)
        binding.imgRecycleView.adapter = null
        binding.tvBottom.text = ""
    }

    override fun onRowClick(pointNum: String, position: Int) {
//        super.onRowClick(pointNum, position)
//        showLog("選擇--點號:$pointNum,位置:$position")
//        binding.tvBottom.text = pointNum
//        imagePickerLauncher.launch("image/*")
        binding.tvBottom.text = pointNum
        binding.imgRecycleView.adapter = ImgAdapter(vm.pointList[position].ImgPath,this)
        BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_EXPANDED
    }

    override fun onDeleteImgClick(imgPaths : MutableList<String>,position: Int) {
        val range = vm.deleteImg(imgPaths,position,binding.tvBottom.text.toString())
        if(position == 0){
            mapManager.updateMapPoint(binding.tvBottom.text.toString(),imgPaths.size>0)
            binding.pointRecycleView.adapter?.notifyItemChanged(vm.pointPosition(binding.tvBottom.text.toString()))
        }
        binding.imgRecycleView.adapter?.notifyItemRemoved(position)
        binding.imgRecycleView.adapter?.notifyItemRangeChanged(position,range)
    }

    private fun mapLoaded() {
        if(vm.currentState.value == UiState.IDLE && mapReady){
            mapManager.initMap(vm.polyJson,vm.pointJson,vm.pointList)
            //多邊形圖層點擊事件
            mapManager.layers[0].setOnFeatureClickListener {
                BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_HIDDEN
            }
            //點圖層點擊事件
            mapManager.layers[1].setOnFeatureClickListener {
                binding.tvBottom.text = it.getProperty("ATTR")
                val position = vm.pointPosition(it.getProperty("ATTR"))
                binding.imgRecycleView.adapter = ImgAdapter(vm.pointList[position].ImgPath,this)
                BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_EXPANDED
            }
            //地圖圖層點擊事件
            mapManager.googleMap.setOnMapClickListener {
                BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_COLLAPSED
            }
        }
    }

    //圖片選擇回調
    private val imagePickerLauncher = this.registerForActivityResult(
        ActivityResultContracts.GetMultipleContents())
        { uriList ->
            try {
                val currentPointNum = binding.tvBottom.text.toString()
                val position = vm.selectImage(uriList,currentPointNum,requireActivity())
                mapManager.updateMapPoint(currentPointNum, vm.pointList[position].ImgPath.size > 0 )
                binding.pointRecycleView.adapter?.notifyItemChanged(position)
                binding.imgRecycleView.adapter = ImgAdapter(vm.pointList[position].ImgPath,this)
                BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_EXPANDED
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), e.message, Toast.LENGTH_SHORT).show()
            }
        }

    //拍照後的回調
    private val cameraLauncher = this.registerForActivityResult(
        ActivityResultContracts.TakePicture() ){  success ->
            try {
                if (success) {
                    val currentPointNum = binding.tvBottom.text.toString()
                    val position = vm.takePhoto(photoData.filePath,currentPointNum,requireActivity())
                    mapManager.updateMapPoint(currentPointNum, vm.pointList[position].ImgPath.size > 0 )
                    binding.pointRecycleView.adapter?.notifyItemChanged(position)
                    binding.imgRecycleView.adapter = ImgAdapter(vm.pointList[position].ImgPath,this)
                    BottomSheetBehavior.from(binding.standardBottomSheet).state = STATE_EXPANDED
                }
                else{
                    Toast.makeText(requireActivity(), "拍照失敗", Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception) {
                showLog("捕獲錯誤:$e")
            }

        }



    override fun onDestroyView() {
        super.onDestroyView()
//        _binding?.mapView?.onDestroy()
        _binding = null
    }



}