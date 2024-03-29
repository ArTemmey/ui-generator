package ru.impression.ui_generator_base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SavedViewState(val superState: Parcelable?, val viewModelState: Parcelable?): Parcelable