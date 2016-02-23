package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.widget.CursorRecyclerViewAdapter;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Show earthquakes as list.
 * 
 *
 */
public class EarthquakeList extends BaseLoaderFragment {

	private static final String KEY_RECYCLER_VIEW_STATE = "KEY_RECYCLER_VIEW_STATE";
	
	private CursorRecyclerViewAdapter mCursorAdapter;
	private RecyclerView mRecyclerView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_earthquake_list, container, false);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mCursorAdapter = new CursorRecyclerViewAdapter(getContext(), null);
		
		mRecyclerView = (RecyclerView) view.findViewById(R.id.earthquake_list);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setAdapter(mCursorAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		LayoutManager layoutManager = null;
		if (mRecyclerView != null) {
			layoutManager = mRecyclerView.getLayoutManager();
		}
		
		if ((layoutManager != null) && (layoutManager instanceof LinearLayoutManager)) {
			outState.putParcelable(KEY_RECYCLER_VIEW_STATE, layoutManager.onSaveInstanceState());
		}
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		
		if (savedInstanceState != null) {
			Parcelable savedRecyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_VIEW_STATE);
			LayoutManager layoutManager = mRecyclerView.getLayoutManager();
			if (layoutManager instanceof LinearLayoutManager) {
				layoutManager.onRestoreInstanceState(savedRecyclerViewState);
			}
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

}
