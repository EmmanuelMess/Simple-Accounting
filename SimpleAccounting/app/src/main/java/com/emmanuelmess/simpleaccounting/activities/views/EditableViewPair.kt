package com.emmanuelmess.simpleaccounting.activities.views

class EditableViewPair<Super, View : Super, Editable : Super>(val normalView: View, val editableView: Editable) {
    var isBeingEdited = true

    fun get(): Super {
        return if (!isBeingEdited) normalView else editableView
    }

}
