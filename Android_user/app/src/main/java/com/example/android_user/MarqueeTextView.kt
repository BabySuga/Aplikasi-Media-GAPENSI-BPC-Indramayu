package com.example.android_user // Sesuaikan dengan nama package Anda

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    // Trik untuk membuat marquee berpikir view ini selalu fokus
    override fun isFocused(): Boolean {
        return true
    }
}