package org.lineageos.updater.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemProperties
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.preference.PreferenceManager
import org.lineageos.updater.R
import org.lineageos.updater.UpdatesCheckReceiver
import org.lineageos.updater.controller.UpdaterService
import org.lineageos.updater.misc.Constants
import org.lineageos.updater.misc.Utils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@SuppressLint("UseSwitchCompatOrMaterialCode")
class PreferenceSheet : BottomSheetDialogFragment() {

    private var prefs: SharedPreferences? = null

    private var mUpdaterService: UpdaterService? = null
    
    private lateinit var preferencesAutoDeleteUpdates: Switch
    private lateinit var preferencesMeteredNetworkWarning: Switch
    private lateinit var preferencesAutoUpdatesCheckInterval: Spinner

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.preferences_dialog, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            preferencesAutoDeleteUpdates = findViewById(R.id.preferences_auto_delete_updates)
            preferencesMeteredNetworkWarning = findViewById(R.id.preferences_metered_network_warning)
            preferencesAutoUpdatesCheckInterval = findViewById(R.id.preferences_auto_updates_check_interval)
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferencesAutoUpdatesCheckInterval.setSelection(Utils.getUpdateCheckSetting(requireContext()))
        preferencesAutoDeleteUpdates.isChecked =
            prefs!!.getBoolean(Constants.PREF_AUTO_DELETE_UPDATES, false)
        preferencesMeteredNetworkWarning.isChecked =
            prefs!!.getBoolean(Constants.PREF_METERED_NETWORK_WARNING,
                prefs!!.getBoolean(Constants.PREF_MOBILE_DATA_WARNING, true))
    }

    override fun onDismiss(dialog: DialogInterface) {
        prefs!!.edit()
            .putInt(
                Constants.PREF_AUTO_UPDATES_CHECK_INTERVAL,
                preferencesAutoUpdatesCheckInterval.selectedItemPosition
            )
            .putBoolean(Constants.PREF_AUTO_DELETE_UPDATES, preferencesAutoDeleteUpdates.isChecked)
            .putBoolean(Constants.PREF_METERED_NETWORK_WARNING, preferencesMeteredNetworkWarning.isChecked)
            .apply()

        if (Utils.isUpdateCheckEnabled(requireContext())) {
            UpdatesCheckReceiver.scheduleRepeatingUpdatesCheck(requireContext())
        } else {
            UpdatesCheckReceiver.cancelRepeatingUpdatesCheck(requireContext())
            UpdatesCheckReceiver.cancelUpdatesCheck(requireContext())
        }

        super.onDismiss(dialog)
    }

    fun setupPreferenceSheet(updaterService: UpdaterService): PreferenceSheet {
        this.mUpdaterService = updaterService
        return this
    }
}
