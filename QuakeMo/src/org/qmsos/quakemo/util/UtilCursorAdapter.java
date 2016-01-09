package org.qmsos.quakemo.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This is a customized Adapter class used on RecyclerView, basically a mock up
 * of SimpleCursorAdapter class .
 * 
 *
 */
public class UtilCursorAdapter extends UtilBaseAdapter<ViewHolder> {

	public UtilCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.US);

		final long id = cursor.getLong(cursor.getColumnIndexOrThrow(QuakeProvider.KEY_ID));
		long time = cursor.getLong(cursor.getColumnIndexOrThrow(QuakeProvider.KEY_TIME));
		double magnitude = cursor.getDouble(cursor.getColumnIndexOrThrow(QuakeProvider.KEY_MAGNITUDE));

		String info = dateFormat.format(new Date(time)) + " - " + "M " + magnitude;
		String details = cursor.getString(cursor.getColumnIndexOrThrow(QuakeProvider.KEY_DETAILS));

		((UtilViewHolder) holder).infoView.setText(info);
		((UtilViewHolder) holder).detailsView.setText(details);

		holder.itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				try {
					((ShowDialogListener) context).onShowDialog(id);
				} catch (ClassCastException e) {
					throw new ClassCastException("context must implement ShowDialogListener");
				}
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder, null);

		return new UtilViewHolder(view);
	}

	public static class UtilViewHolder extends ViewHolder {
		TextView infoView;
		TextView detailsView;

		public UtilViewHolder(View itemView) {
			super(itemView);

			infoView = (TextView) itemView.findViewById(R.id.info);
			detailsView = (TextView) itemView.findViewById(R.id.details);
		}
	}

	/**
	 * Interface for a callback to be invoked when a view of ViewHolder class is
	 * called.
	 * 
	 *
	 */
	public interface ShowDialogListener {
		/**
		 * Callback to show dialog of earthquake.
		 * 
		 * @param id
		 *            The ID of earthquake.
		 */
		void onShowDialog(long id);
	}

}
