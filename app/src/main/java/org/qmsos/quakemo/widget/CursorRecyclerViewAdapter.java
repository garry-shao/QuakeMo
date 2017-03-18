package org.qmsos.quakemo.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.contract.ProviderContract.Entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This is a customized Adapter class used on RecyclerView, basically a mock up
 * of SimpleCursorAdapter class.
 */
public class CursorRecyclerViewAdapter extends BaseCursorRecyclerViewAdapter<ViewHolder> {

    private OnViewHolderClickedListener mListener;

    public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);

        try {
            mListener = (OnViewHolderClickedListener) context;
        } catch (ClassCastException e) {
            String listenerName = OnViewHolderClickedListener.class.getSimpleName();

            throw new ClassCastException(context.toString()
                    + " must implements "
                    + listenerName);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final long earthquakeId = cursor.getLong(
				cursor.getColumnIndexOrThrow(Entity.ID));
        long time = cursor.getLong(
				cursor.getColumnIndexOrThrow(Entity.TIME));
        double magnitude = cursor.getDouble(
                cursor.getColumnIndexOrThrow(Entity.MAGNITUDE));
        String details = cursor.getString(
                cursor.getColumnIndexOrThrow(Entity.DETAILS));

        DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.US);
        String timeString = dateFormat.format(new Date(time));

        String info = timeString + " - " + "M " + magnitude;

        ((RecyclerViewHolder) holder).mInfoView.setText(info);
        ((RecyclerViewHolder) holder).mDetailsView.setText(details);

        holder.itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onViewHolderClicked(earthquakeId);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.view_holder, null);

        return new RecyclerViewHolder(view);
    }

    static class RecyclerViewHolder extends ViewHolder {
        TextView mInfoView;
        TextView mDetailsView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            mInfoView = (TextView) itemView.findViewById(R.id.info);
            mDetailsView = (TextView) itemView.findViewById(R.id.details);
        }
    }

    /**
     * Interface for a callback to be invoked when a view of ViewHolder class is
     * called.
     */
    public interface OnViewHolderClickedListener {
        /**
         * Callback when specific ViewHolder is clicked.
         *
         * @param earthquakeId
         *            The ID of the earthquake in ViewHolder.
         */
        void onViewHolderClicked(long earthquakeId);
    }
}