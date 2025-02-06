package com.olam.warehouse.login.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.presentation.enums.CountryCode
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.PreferenceHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var preferenceKey = getPreferenceKey()
        addPreferencesFromResource(R.xml.preferences)
        bindPreferenceSummaryToValue(
            findPreference(preferenceKey),
            requireActivity()
        )
    }

    private fun getPreferenceKey(): String {
        return when (PreferenceHelper.get(Constants.COUNTRY_CODE, "")) {
            CountryCode.PERU.code -> "listSpanish"
            CountryCode.GUATEMALA.code -> "listSpanish"
            CountryCode.VIETNAM.code -> "listVietnamese"
            CountryCode.HONDURAS.code -> "listSpanish"
            CountryCode.INDONESIA.code -> "listBahasa"
            CountryCode.IVC.code -> "listIvc"
            CountryCode.ECUADOR.code -> "listSpanish"
            CountryCode.COLUMBIA.code -> "listSpanish"
            CountryCode.NICARAGUA.code -> "listSpanish"
            CountryCode.TOGO.code -> "listIvc"
            else -> "list"
        }
    }

    companion object {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */

        private fun bindPreferenceSummaryToValue(preference: Preference?, context: FragmentActivity) {
            preference?.isVisible = true
            val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { prefer, value ->

                var language = value.toString()

                if (prefer is ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val mLanguage = Locale.getDefault().language
                    if (mLanguage.equals("en") && !language.equals("en")) {
                        language = "en"
                        prefer.setValueIndex(0)
                    }
                    val index = prefer.findIndexOfValue(language)

                    // Set the summary to reflect the new value.
                    prefer.setSummary(if (index >= 0) prefer.entries[index] else "Click to show a list to choose from")

                    //val country = if (language == "en") "US" else "ES"
                    //LocaleHelper.setLocale(context, Locale(language, ""))
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    prefer.summary = language
                }
                true
            }
            preference?.let {
                // Set the listener to watch for value changes.
                //it.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
                // Trigger the listener immediately with the preference's
                // current value.
                sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference, PreferenceManager
                        .getDefaultSharedPreferences(it.context)
                        .getString(it.key, "Click to show a list to choose from")
                )
                it.setOnPreferenceChangeListener { preference, newValue ->

                    val language = newValue.toString()
                    val shouldRecreate = language != LocaleHelper.getLocale(context).language

                    if (preference is ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        val index = preference.findIndexOfValue(language)

                        // Set the summary to reflect the new value.
                        preference.setSummary(if (index >= 0) preference.entries[index] else "Click to show a list to choose from")

                        //val country = if (language == "en") "US" else "ES"
                        if (shouldRecreate) {
                            LocaleHelper.setLocale(context, Locale(language, ""))
                            PreferenceHelper.save("languageCode", language)
                            val intent = Intent(context, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            context.finish()
                        }

                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.summary = language
                    }
                    true
                }
            }
        }
    }
}
