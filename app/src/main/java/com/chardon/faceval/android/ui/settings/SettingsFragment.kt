package com.chardon.faceval.android.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.chardon.faceval.android.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}