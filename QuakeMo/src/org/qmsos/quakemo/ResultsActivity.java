package org.qmsos.quakemo;

import org.qmsos.quakemo.fragment.ResultsListFragment;
import org.qmsos.quakemo.fragment.ResultsMapFragment;
import org.qmsos.quakemo.fragment.QuakeDetailsDialog.ShowMapListener;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * The activity show search results.
 *
 *
 */
public class ResultsActivity extends AppCompatActivity implements ShowMapListener {

	/**
	 * Key used to pass query string in bundle.
	 */
	public static final String KEY_QUERY = "KEY_QUERY";

	/**
	 * TAG used to track fragment of search results.
	 */
	private static final String TAG_LIST = "TAG_LIST";
	
	/**
	 * TAG used to track fragment of single earthquake in map.
	 */
	private static final String TAG_MAP = "TAG_MAP";
	
	/**
	 * TAG used track weather the search intent has executed at least once.
	 */
	private static final String TAG_FIRST = "TAG_FIRST";
	
	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		setContentView(R.layout.activity_results);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		if (args == null || args.getBoolean(TAG_FIRST)) {
			parseIntent(getIntent());
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		parseIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		FragmentManager manager = getSupportFragmentManager();
		if (manager.findFragmentByTag(TAG_LIST) == null) {
			outState.putBoolean(TAG_FIRST, true);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onShowMap(Location location) {
		ResultsMapFragment map = new ResultsMapFragment();
		if (location != null) {
			map.setLocation(location);
		}
		
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.layout_fragment_container, map, TAG_MAP);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	/**
	 * Parse and process any search intent.
	 * 
	 * @param intent
	 *            The intent to parse.
	 */
	private void parseIntent(Intent intent) {
		ResultsListFragment searchFragment = new ResultsListFragment();
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			Bundle args = new Bundle();
			args.putString(KEY_QUERY, query);

			searchFragment.setArguments(args);
		}

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.layout_fragment_container, searchFragment, TAG_LIST);
		transaction.commit();
	}

}
