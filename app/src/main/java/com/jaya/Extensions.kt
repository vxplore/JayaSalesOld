package com.jaya

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.jaya.web.CustomWebView

val View.bitmap: Bitmap
    get(){
        if(this is CustomWebView){
            /*measure(
                MeasureSpec.makeMeasureSpec(
                    MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED
                ),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            layout(0, 0, getMeasuredWidth(), getMeasuredHeight())*/
            setDrawingCacheEnabled(true)
            buildDrawingCache()
            val bitmap = Bitmap.createBitmap(
                /*getMeasuredWidth()*/width,
                /*getMeasuredHeight()*/height, Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            val paint = Paint()
            val iHeight = bitmap.height
            canvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
            draw(canvas)
            return bitmap
        }
        else{
            val b = Bitmap.createBitmap(
                /*layoutParams.*/width,
                /*layoutParams.*/height,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            layout(left, top, right, bottom)
            draw(c)
            return b
        }
    }