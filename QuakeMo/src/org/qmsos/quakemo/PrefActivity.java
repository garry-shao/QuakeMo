package org.qmsos.quakemo;

import java.util.List;

import org.qmsos.quakemo.fragment.PrefGraphicFragment;
import org.qmsos.quakemo.fragment.PrefRefreshFragment;
import org.qmsos.quakemo.util.UtilDialogPreference.PositiveClickListener;
import org.qmsos.quakemo.util.UtilPreferenceActivity;
import org.qmsos.quakemo.util.UtilResultReceiver;
import org.qmsos.quakemo.util.UtilResultReceiver.Receiver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * The main activity managing preferences.
 *
 */
public class PrefActivity extends UtilPreferenceActivity 
implements OnSharedPreferenceChangeListener, Receiver, PositiveClickListener {

	private static final String KEY_RECEIVER = "KEY_RECEIVER";

	/**
	 * Callback from update service.
	 */
	private UtilResultReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			receiver = savedInstanceState.getParcelable(KEY_RECEIVER);
		} else {
			receiver = new UtilResultReceiver(new Handler());
		}
	}

	@Override
	protected void onResume() {
		receiver.setReceiver(this);

		super.onResume();
	}

	@Override
	protected void onPause() {
		receiver.setReceiver(null);

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_RECEIVER, receiver);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
		
		setContentView(R.layout.activity_pref);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return PrefGraphicFragment.class.getName().equals(fragmentName) || 
				PrefRefreshFragment.class.getName().equals(fragmentName);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.PREF_WIDGET))) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			int newState = prefs.getBoolean(key, false) ? 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
			
			PackageManager manager = getPackageManager();
			manager.setComponentEnabledSetting(
					new ComponentName(this, QuakeWidgetSingleton.class), 
					newState, PackageManager.DONT_KILL_APP);
			manager.setComponentEnabledSetting(
					new ComponentName(this, QuakeWidgetList.class), 
					newState, PackageManager.DONT_KILL_APP);
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		View linearLayout = findViewById(R.id.linear_layout);
		if (linearLayout != null) {
			Snackbar.make(linearLayout, R.string.snackbar_purged, Snackbar.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onPositiveClick() {
		final Intent i = new Intent(this, QuakeUpdateService.class);
		i.putExtra(QuakeUpdateService.PURGE_DATABASE, true);
		i.putExtra(UtilResultReceiver.RECEIVER, receiver);
				
		View linearLayout = findViewById(R.id.linear_layout);
		if (linearLayout != null) {
			final Snackbar snackbar = 
					Snackbar.make(linearLayout, R.string.snackbar_purging, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.snackbar_undo, new OnClickListener() {

				@Override
				public void onClick(View v) {
					snackbar.dismiss();
				}
			});
			snackbar.setCallback(new Callback() {

				@Override
				public void onDismissed(Snackbar snackbar, int event) {
					if (event != Callback.DISMISS_EVENT_ACTION && event != Callback.DISMISS_EVENT_MANUAL) {
						startService(i);
					}
					
					super.onDismissed(snackbar, event);
				}
			});
			
			snackbar.show();
		}
	}

}
