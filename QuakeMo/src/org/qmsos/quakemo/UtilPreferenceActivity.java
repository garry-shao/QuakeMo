package org.qmsos.quakemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Modified from google's sample AppCompatPreferenceActivity.
 * 
 *
 */
public class UtilPreferenceActivity extends PreferenceActivity {

	/**
	 * Add AppCompat ability to any activity.
	 */
	private AppCompatDelegate delegate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getDelegate().installViewFactory();
		getDelegate().onCreate(savedInstanceState);
		
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		getDelegate().onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		getDelegate().onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		
		getDelegate().onPostResume();
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		super.onTitleChanged(title, color);
		
		getDelegate().setTitle(title);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		getDelegate().onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		getDelegate().onDestroy();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		
		getDelegate().setContentView(layoutResID);
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		
		getDelegate().setContentView(view);
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		
		getDelegate().setContentView(view, params);
	}

	@Override
	public MenuInflater getMenuInflater() {
		return getDelegate().getMenuInflater();
	}

	@Override
	public void invalidateOptionsMenu() {
		getDelegate().invalidateOptionsMenu();
	}

	public void setSupportActionBar(Toolbar toolbar) {
		getDelegate().setSupportActionBar(toolbar);
	}
	
	public ActionBar getSupportActionBar() {
		return getDelegate().getSupportActionBar();
	}
	
	private AppCompatDelegate getDelegate() {
		if (delegate == null) {
			delegate = AppCompatDelegate.create(this, null);
		}
		return delegate;
    }
}
