package com.sensors.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sensors.app.R
import com.sensors.app.app.App
import com.sensors.app.databinding.FragmentMapsBinding
import com.sensors.app.viewmodel.MapsViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.TextStyle

class MapsFragment : Fragment() {
    private var binding: FragmentMapsBinding? = null
    private var viewModel: MapsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        MapKitFactory.initialize(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding
        .inflate(inflater, container, false)
        .also { binding = it }
        .root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: MapsFragmentArgs by navArgs()
        viewModel = viewModels<MapsViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return (requireActivity().application as App).appComponent.mapsViewModel() as T
                }
            }
        }.value

        val map = binding?.map?.mapWindow?.map
        map?.isNightModeEnabled = viewModel?.isDarkTheme == true
        map?.setMapLoadedListener {
            args.location?.let { locationPoint ->
                map.move(
                    CameraPosition(
                        /* target = */ locationPoint.asMapPoint(),
                        /* zoom = */ map.cameraBounds.maxZoom * .95f,
                        /* azimuth = */ locationPoint.azimuth,
                        /* tilt = */ 70f
                    ),
                    Animation(Animation.Type.SMOOTH, 5f),
                    null
                )

                map.mapObjects.addPlacemark(locationPoint.asMapPoint()).apply {
                    setText(
                        getString(R.string.you_are_here),
                        TextStyle(
                            /* size = */ 20f,
                            /* color = */ requireContext().getColor(R.color.text_color),
                            /* outlineWidth = */ 0f,
                            /* outlineColor = */ null,
                            /* placement = */ TextStyle.Placement.BOTTOM,
                            /* offset = */ 0f,
                            /* offsetFromIcon = */ true,
                            /* textOptional = */ false
                        ),
                    )

                    setIconStyle(
                        IconStyle(
                            /* anchor = */ null,
                            /* rotationType = */ RotationType.NO_ROTATION,
                            /* zIndex = */ null,
                            /* flat = */ true,
                            /* visible = */ true,
                            /* scale = */ 4f,
                            /* tappableArea = */ null
                        )
                    )
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding?.map?.onStart()
    }

    override fun onStop() {
        binding?.map?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding = null
        viewModel = null
        super.onDestroy()
    }
}