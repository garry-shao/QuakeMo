package org.qmsos.quakemo;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * 
 * The activity show search results.
 *
 */
public class MainSearchActivity extends FragmentActivity  {
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		parseIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		parseIntent(intent);
	}

	/**
	 * Parse and process any search intent.
	 * @param intent The intent to parse.
	 */
	private void parseIntent(Intent intent) {
		EarthquakeSearchFragment searchFragment = new EarthquakeSearchFragment();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			
			Bundle args = new Bundle();
			args.putString(EarthquakeSearchFragment.QUERY_EXTRA_KEY, searchQuery);
			
			searchFragment.setArguments(args);
		}
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(android.R.id.content, searchFragment);
		transaction.commit();
	}

}
