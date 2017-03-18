package org.qmsos.quakemo.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.widget.CursorRecyclerViewAdapter;

/**
 * Show earthquakes as list.
 */
public class EarthquakeList extends BaseLoaderFragment {

    private CursorRecyclerViewAdapter mCursorAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earthquake_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCursorAdapter = new CursorRecyclerViewAdapter(getContext(), null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.earthquake_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mCursorAdapter);
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