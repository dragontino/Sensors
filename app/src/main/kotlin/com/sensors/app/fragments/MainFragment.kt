@file:Suppress("DEPRECATION")

package com.sensors.app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.sensors.app.R
import com.sensors.app.app.App
import com.sensors.app.databinding.FragmentMainBinding
import com.sensors.app.model.LocationPoint
import com.sensors.app.util.Cell
import com.sensors.app.util.RequestCodes
import com.sensors.app.util.onRequestPermissionsResult
import com.sensors.app.util.reversedColor
import com.sensors.app.util.usePermissions
import com.sensors.app.viewmodel.MainViewModel
import com.sensors.domain.model.Accelerometer
import com.sensors.domain.model.Gyroscope


class MainFragment : Fragment(), SensorEventListener, MenuProvider {

    private var binding: FragmentMainBinding? = null
    private var sensorManager: SensorManager? = null
    private var telephonyManager: TelephonyManager? = null

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    @RequiresApi(Build.VERSION_CODES.S)
    private var telephonyCallback: TelephonyCallback? = null
    private var phoneStateListener: PhoneStateListener? = null

    private var viewModel: MainViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = viewModels<MainViewModel> {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return (requireActivity().application as App).appComponent.mainViewModel() as T
                }
            }
        }.value

        context?.getColor(R.color.orange)?.let {
            binding?.toolbar?.setBackgroundColor(it)
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding!!.toolbar) { toolbar, insets ->
            val toolbarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            toolbar.setPadding(toolbarInsets.left, toolbarInsets.top, toolbarInsets.right, toolbarInsets.bottom)
            insets
        }

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding!!.toolbar)
        requireActivity().addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        setupViews()

        sensorManager = context?.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        telephonyManager = context?.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        viewModel?.onLocationUpdated = {
            binding?.locationInfo?.textView?.text = viewModel?.locationText
        }


        onRequestPermissionsResult(
            grantedPermissions = { permissions ->
                println(permissions.joinToString(" | "))
                if (
                    Manifest.permission.ACCESS_FINE_LOCATION in permissions &&
                    Manifest.permission.ACCESS_COARSE_LOCATION in permissions
                ) {
                    startLocationUpdates()
                }

                if (
                    Manifest.permission.ACCESS_FINE_LOCATION in permissions &&
                    Manifest.permission.READ_PHONE_STATE in permissions
                ) {
                    startCellsUpdates()
                }
            },
            deniedPermissions = { permissions ->
                viewModel?.deniedPermissions?.addAll(permissions)
                Toast.makeText(
                    context?.applicationContext,
                    "Permissions ${permissions.joinToString(" ")} was denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }


    private fun setupViews() {
        fun notifyAboutSuccess(path: String) {
            activity?.runOnUiThread {
                showSnackbar(getString(R.string.data_saved, path))
            }
        }
        fun notifyAboutFailture(message: String) {
            activity?.runOnUiThread {
                showSnackbar(message, action = getString(R.string.close_action))
            }
        }

        binding?.apply {
            accelerometerInfo.textView.hint = context?.getString(R.string.accelerometer_hint)
            gyroscopeInfo.textView.hint = context?.getString(R.string.gyroscope_hint)
            locationInfo.textView.hint = context?.getString(R.string.location_hint)
            cellInfo.textView.hint = context?.getString(R.string.cell_hint)

            accelerometerInfo.textView.text = viewModel?.accelerometerText
            gyroscopeInfo.textView.text = viewModel?.gyroscopeText
            locationInfo.textView.text = viewModel?.locationText
            cellInfo.textView.text = viewModel?.cellText

            accelerometerInfo.trailingAction.setOnClickListener {
                usePermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    requestCode = RequestCodes.REQUEST_EXTERNAL_STORAGE_PERMISSION
                ) {
                    viewModel?.writeAccelerometerData(
                        onSuccess = ::notifyAboutSuccess,
                        onFailure = ::notifyAboutFailture
                    )
                }
            }
            gyroscopeInfo.trailingAction.setOnClickListener {
                usePermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    requestCode = RequestCodes.REQUEST_EXTERNAL_STORAGE_PERMISSION
                ) {
                    viewModel?.writeGyroscopeData(
                        onSuccess = ::notifyAboutSuccess,
                        onFailure = ::notifyAboutFailture
                    )
                }
            }
            locationInfo.trailingAction.setOnClickListener {
                usePermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    requestCode = RequestCodes.REQUEST_EXTERNAL_STORAGE_PERMISSION
                ) {
                    viewModel?.writeLocationData(
                        onSuccess = ::notifyAboutSuccess,
                        onFailure = ::notifyAboutFailture
                    )
                }
            }
            cellInfo.trailingAction.setOnClickListener {
                usePermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    requestCode = RequestCodes.REQUEST_EXTERNAL_STORAGE_PERMISSION
                ) {
                    viewModel?.writeCellData(
                        onSuccess = ::notifyAboutSuccess,
                        onFailure = ::notifyAboutFailture
                    )
                }
            }

            saveAllInfoButton.setOnClickListener {
                usePermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    requestCode = RequestCodes.REQUEST_EXTERNAL_STORAGE_PERMISSION
                ) {
                    viewModel?.writeAllData(
                        onSuccess = {
                            activity?.runOnUiThread {
                                showSnackbar(
                                    message = getString(R.string.all_data_saved),
                                    action = getString(R.string.close_action)
                                )
                            }
                        },
                        onFailure = ::notifyAboutFailture
                    )
                }
            }
        }
    }


    private fun showSnackbar(message: String, action: String? = null) {
        val parentLayout = binding?.root ?: return
        val duration = if (action == null) Snackbar.LENGTH_LONG else Snackbar.LENGTH_INDEFINITE
        Snackbar
            .make(parentLayout, message, duration)
            .apply {
                if (action != null) {
                    setAction(action) { dismiss() }
                }
            }
            .setBackgroundTint(requireContext().getColor(R.color.background_color).reversedColor)
            .setActionTextColor(requireContext().getColor(R.color.orange))
            .show()
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_fragment_menu, menu)
        val openMapItem = menu.findItem(R.id.menu_action_open_map)
        openMapItem.iconTintList = ColorStateList.valueOf(requireContext().getColor(R.color.text_color))
    }


    override fun onResume() {
        super.onResume()
        registerSensorListener()
        startLocationUpdates()
        startCellsUpdates()
    }


    override fun onPause() {
        sensorManager?.unregisterListener(this)
        stopLocationUpdates()
        stopCellsUpdates()
        super.onPause()
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_action_open_map -> {
                navigateToMapsFragment()
                true
            }
            else -> false
        }
    }


    private fun navigateToMapsFragment() {
        val locationPoint = viewModel?.location?.let(::LocationPoint)
        val action = MainFragmentDirections.openMapsAction(locationPoint)
        findNavController().navigate(action)
    }



    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (
            Manifest.permission.ACCESS_FINE_LOCATION in viewModel!!.deniedPermissions ||
            Manifest.permission.ACCESS_COARSE_LOCATION in viewModel!!.deniedPermissions
        ) {
            return
        }

        usePermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            requestCode = RequestCodes.REQUEST_LOCATION_PERMISSION,
        ) {
            fusedLocationProviderClient?.requestLocationUpdates(
                viewModel!!.locationRequest,
                viewModel!!.locationCallback,
                null
            )
            viewModel?.locationUpdatesStarted = true
        }
    }


    private fun stopLocationUpdates() {
        viewModel
            ?.locationCallback
            ?.takeIf { viewModel?.locationUpdatesStarted == true }
            ?.let { fusedLocationProviderClient?.removeLocationUpdates(it) }
    }


    private fun startCellsUpdates() {
        if (
            Manifest.permission.ACCESS_FINE_LOCATION in viewModel!!.deniedPermissions ||
            Manifest.permission.READ_PHONE_STATE in viewModel!!.deniedPermissions
        ) {
            return
        }

        fun onCellInfoChange(data: List<CellInfo>) {
            viewModel?.cellData = data.firstOrNull { it.isRegistered }?.let(::Cell)
            binding?.cellInfo?.textView?.text = viewModel?.cellText
        }


        usePermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            requestCode = RequestCodes.REQUEST_PHONE_STATE_PERMISSION
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback =
                    object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
                        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                            onCellInfoChange(cellInfo)
                        }
                    }

                telephonyManager?.registerTelephonyCallback(
                    /* executor = */requireContext().mainExecutor,
                    /* callback = */telephonyCallback!!
                )
            } else {
                phoneStateListener = object : PhoneStateListener() {
                    @Deprecated("", ReplaceWith("cellInfo?.let(::onCellInfoChange)"))
                    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                        cellInfo?.let(::onCellInfoChange)
                    }
                }
                telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_INFO)
            }

            viewModel?.cellUpdatesStarted = true
        }
    }


    private fun stopCellsUpdates() {
        if (viewModel?.cellUpdatesStarted == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback?.let { telephonyManager?.unregisterTelephonyCallback(it) }
                telephonyCallback = null
            } else {
                telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
                phoneStateListener = null
            }
        }
    }


    private fun registerSensorListener() {
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager?.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onDestroy() {
        binding = null
        viewModel = null
        super.onDestroy()
    }


    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                viewModel?.accelerometerData = Accelerometer(
                    x = event.values[0],
                    y = event.values[1],
                    z = event.values[2]
                )
                binding?.accelerometerInfo?.textView?.text = viewModel?.accelerometerText
            }

            Sensor.TYPE_GYROSCOPE -> {
                viewModel?.gyroscopeData = Gyroscope(
                    x = event.values[0],
                    y = event.values[1],
                    z = event.values[2]
                )
                binding?.gyroscopeInfo?.textView?.text = viewModel?.gyroscopeText
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}