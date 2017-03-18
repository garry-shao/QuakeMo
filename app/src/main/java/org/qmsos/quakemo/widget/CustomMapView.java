package org.qmsos.quakemo.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Customized MapView class that restores extra information when configuration changed. 
 */
public class CustomMapView extends MapView {

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMapView(Context context) {
        super(context);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        GeoPoint center = (GeoPoint) getMapCenter();
        int zoomLevel = getZoomLevel();

        SavedState savedState = new SavedState(superState);
        savedState.mCenter = center;
        savedState.mZoomLevel = zoomLevel;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.mCenter != null && savedState.mZoomLevel >= 0) {
            getController().setCenter(savedState.mCenter);
            getController().setZoom(savedState.mZoomLevel);
        }
    }

    /**
     * Used as saved state that passed when saving and restoring instance state.
     */
    static class SavedState extends BaseSavedState {
        private GeoPoint mCenter;
        private int mZoomLevel;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(mCenter, 0);
            out.writeInt(mZoomLevel);

            super.writeToParcel(out, flags);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel source) {
            super(source);

            mCenter = source.readParcelable(getClass().getClassLoader());
            mZoomLevel = source.readInt();
        }
    }
}