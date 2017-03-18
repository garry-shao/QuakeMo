package org.qmsos.quakemo.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

/**
 * Customized Toolbar class that keeping title in SavedState.
 */
public class CustomToolbar extends Toolbar {

    public CustomToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomToolbar(Context context) {
        super(context);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        String title = getTitle().toString();

        SavedState savedState = new SavedState(superState);
        savedState.mTitle = title;

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

        if (savedState.mTitle != null) {
            setTitle(savedState.mTitle);
        }
    }

    /**
     * Used as saved state that passed when saving and restoring instance state.
     */
    static class SavedState extends BaseSavedState {
        private String mTitle;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mTitle);

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

            mTitle = source.readString();
        }
    }
}