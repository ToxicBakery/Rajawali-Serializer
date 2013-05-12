package com.ToxicBakery.apps.rajawaliserializer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingFragment()).commit();
	}

	public static final class SettingFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.settings);

			Intent intent;

			// Find more Toxic Bakery apps
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://search?q=pub:Toxic Bakery"));
			final Preference prefMoreApps = findPreference(getString(R.string.preference_about_more_apps_key));
			prefMoreApps.setIntent(intent);

			// Rate the application
			intent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("market://details?id=com.ToxicBakery.apps.rajawaliserializer"));
			final Preference prefRateUs = findPreference(getString(R.string.preference_about_rate_us_key));
			prefRateUs.setIntent(intent);

			// Learn about Rajawali
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://github.com/MasDennis/Rajawali"));
			final Preference prefRajawali = findPreference(getString(R.string.preference_rajawali_learn_key));
			prefRajawali.setIntent(intent);

			// Fork us on Github
			intent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://github.com/ToxicBakery/Rajawali-Serializer"));
			final Preference prefFork = findPreference(getString(R.string.preference_github_fork_key));
			prefFork.setIntent(intent);
		}
	}

}
