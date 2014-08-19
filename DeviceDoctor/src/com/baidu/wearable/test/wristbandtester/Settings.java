package com.baidu.wearable.test.wristbandtester;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.baidu.wifi.R;

public class Settings extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {
		private static final int REQUEST_SELECT_FILE = 0;
		@Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			addPreferencesFromResource(R.xml.preferences);
			Preference filePicker = (Preference) findPreference(getResources().getString(R.string.key_ota));
			String def = filePicker.getSharedPreferences().getString(getResources().getString(R.string.key_ota), null);
			filePicker.setSummary(def);
		    filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		        @Override
		        public boolean onPreferenceClick(Preference preference) {
		        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		    		intent.setType("file/*.bin");
		    		intent.addCategory(Intent.CATEGORY_OPENABLE);
//		    		intent.setClass(getActivity(), FileExplorerTabActivity.class);
		    		startActivityForResult(intent, REQUEST_SELECT_FILE);
		            return true;
		        }
		    });
		}
		@Override
		public void onActivityResult(int request, int result, Intent data) {
			String value = null;
			if (request == REQUEST_SELECT_FILE && result==Activity.RESULT_OK) {
				final Uri uri = data.getData();
				/*
				 * The URI returned from application may be in 'file' or 'content' schema.
				 * 'File' schema allows us to create a File object and read details from if directly.
				 * 
				 * Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
				 */
				if (uri.getScheme().equals("file")) {
					// the direct path to the file has been returned
					final String path = uri.getPath();
					boolean isBinFile = MimeTypeMap.getFileExtensionFromUrl(path).equalsIgnoreCase("bin");

					if (!isBinFile) {
						Toast.makeText(getActivity(), "File invalid", Toast.LENGTH_LONG).show();
					} else {
						value = path;
					}
				}
			}
			Preference filePicker = (Preference) findPreference(getResources().getString(R.string.key_ota));
			filePicker.setSummary(value);
			filePicker.getEditor().putString(filePicker.getKey(), value).commit();
		}
	}
}
