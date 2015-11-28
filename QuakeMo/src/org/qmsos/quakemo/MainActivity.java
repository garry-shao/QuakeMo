package org.qmsos.quakemo;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.SearchView.OnSuggestionListener;

/**
 * 
 * Main activity of this application.
 *
 */
public class MainActivity extends FragmentActivity {

	protected static final int SHOW_PREFERENCES = 1;
	
	/**
	 * Stored fragments used for page flip.
	 */
	private List<Fragment> fragmentList = new ArrayList<Fragment>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fragmentList.add(new QuakeListFragment());
		fragmentList.add(new QuakeMapFragment());

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(new UtilPagerAdapter(getSupportFragmentManager(), fragmentList));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		getMenuInflater().inflate(R.menu.menu_main_options, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		searchView.setOnSuggestionListener(new OnSuggestionListener() {

			@Override
			public boolean onSuggestionSelect(int position) {
				return false;
			}
			
			@Override
			public boolean onSuggestionClick(int position) {
				Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
				String suggestion = cursor.getString(
						cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
				
				searchView.setQuery(suggestion, true);
				
				return true;
			}
		});
	
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == SHOW_PREFERENCES) {
			startService(new Intent(this, QuakeUpdateService.class));
		}
	}

}
