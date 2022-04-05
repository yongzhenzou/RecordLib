package com.example.recordlib

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    )

fun Int.toDp(): Float {
    val scale =  Resources.getSystem().displayMetrics.density
    return this.toFloat() / scale + 0.5f
}

val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )

val Int.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics)