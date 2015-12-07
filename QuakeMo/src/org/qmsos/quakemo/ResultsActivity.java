package org.qmsos.quakemo;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * 
 * The activity show search results.
 *
 */
public class ResultsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		setContentView(R.layout.activity_results);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		parseIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		parseIntent(intent);
	}

	/**
	 * Parse and process any search intent.
	 * 
	 * @param intent
	 *            The intent to parse.
	 */
	private void parseIntent(Intent intent) {
		QuakeSearchFragment quakeSearch = new QuakeSearchFragment();
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);

			Bundle args = new Bundle();
			args.putString(QuakeSearchFragment.QUERY_EXTRA_KEY, searchQuery);

			quakeSearch.setArguments(args);
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.layout_fragment_container, quakeSearch);
		transaction.commit();
	}

}
