package com.emmanuelmess.simpleaccounting.activities.views;

import android.text.Editable;
import android.view.View;

public final class EditableViewPair<Super, View extends Super, Editable extends Super> {
    public final View normalView;
    public final Editable editableView;
    private boolean isBeingEdited = true;

    public EditableViewPair(View view, Editable editable) {
        normalView = view;
        editableView = editable;
    }

    public Super get() {
        return !isBeingEdited? normalView:editableView;
    }

    public void setBeingEdited(boolean beingEdited) {
        isBeingEdited = beingEdited;
    }

    public boolean isBeingEdited() {
        return isBeingEdited;
    }

}
