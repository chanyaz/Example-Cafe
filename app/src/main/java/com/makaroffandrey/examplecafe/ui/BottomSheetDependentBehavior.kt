package com.makaroffandrey.examplecafe.ui

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.makaroffandrey.examplecafe.R

/**
 * This behavior allows to modify size and position of the view based on the current position of the bottom sheet
 * The behavior remembers two positions of the view: one dictated by its layout_* attributes,
 * The other is dictated by corresponding sheet_* attributes.
 * Currently not all attributes are supported, only the ones that were needed to implement the layout
 */
@Suppress("unused") //used inside the activity_main layout for some reason Studio thinks it is unused
class BottomSheetDependentBehavior<T : View>(context: Context,
                                             attrs: AttributeSet) : CoordinatorLayout.Behavior<T>(
        context, attrs) {

    private var bottomSheetOffset: Float = 0f
    private var initialLeft = 0
    private var targetLeft = 0
    private var initialTop = 0
    private var targetTop = 0
    private var initialHeight = 0
    private var targetHeight = 0
    private var layoutMarginTop = 0
    private var sheetMarginTop = 0
    private var layoutMarginBottom = 0
    private var sheetMarginBottom = 0
    private var layoutAnchorGravity = Gravity.NO_GRAVITY
    private var sheetAnchorGravity = Gravity.NO_GRAVITY
    private var layoutGravity = Gravity.NO_GRAVITY
    private var sheetGravity = Gravity.NO_GRAVITY
    private var layoutHeight = 0
    private var sheetHeight = 0
    private var widthMeasureSpec = 0
    private var widthUsed = 0
    private var heightUsed = 0
    private val internalCallback = Callback(
            this)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetDependent_Layout)
        layoutMarginTop = a.getDimensionPixelOffset(
                R.styleable.BottomSheetDependent_Layout_android_layout_marginTop, 0)
        sheetMarginTop = a.getDimensionPixelOffset(
                R.styleable.BottomSheetDependent_Layout_sheet_marginTop, layoutMarginTop)
        layoutMarginBottom = a.getDimensionPixelOffset(
                R.styleable.BottomSheetDependent_Layout_android_layout_marginBottom, 0)
        sheetMarginBottom = a.getDimensionPixelOffset(
                R.styleable.BottomSheetDependent_Layout_sheet_marginBottom, layoutMarginTop)
        layoutAnchorGravity = a.getInteger(R.styleable.BottomSheetDependent_Layout_layout_anchorGravity,
                Gravity.NO_GRAVITY)
        sheetAnchorGravity = a.getInteger(
                R.styleable.BottomSheetDependent_Layout_sheet_anchorGravity, layoutAnchorGravity)
        layoutGravity = a.getInteger(R.styleable.BottomSheetDependent_Layout_android_layout_gravity,
                Gravity.NO_GRAVITY)
        sheetGravity = a.getInteger(R.styleable.BottomSheetDependent_Layout_sheet_gravity,
                layoutGravity)
        sheetHeight = a.getDimensionPixelSize(R.styleable.BottomSheetDependent_Layout_sheet_height,
                0)

        a.recycle()
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: T?,
                                        dependency: View?): Boolean {
        if (initialHeight != targetHeight) {
            child?.layoutParams?.height = initialHeight + (bottomSheetOffset * (targetHeight - initialHeight)).toInt()
            parent?.onMeasureChild(child, widthMeasureSpec, widthUsed,
                    View.MeasureSpec.makeMeasureSpec(
                            initialHeight + (bottomSheetOffset * (targetHeight - initialHeight)).toInt(),
                            View.MeasureSpec.EXACTLY), heightUsed)
            parent?.onLayoutChild(child, ViewCompat.LAYOUT_DIRECTION_LTR)
        }
        child?.translationX = bottomSheetOffset * (targetLeft - initialLeft)
        child?.translationY = bottomSheetOffset * (targetTop - initialTop)
        return true
    }

    private fun CoordinatorLayout.LayoutParams.setLayout() {
        this.topMargin = layoutMarginTop
        this.bottomMargin = layoutMarginBottom
        this.anchorGravity = layoutAnchorGravity
        this.gravity = layoutGravity
        this.height = layoutHeight
    }

    private fun CoordinatorLayout.LayoutParams.setSheet() {
        this.topMargin = sheetMarginTop
        this.bottomMargin = sheetMarginBottom
        this.anchorGravity = sheetAnchorGravity
        this.gravity = sheetGravity
        this.height = sheetHeight
    }

    override fun onMeasureChild(parent: CoordinatorLayout?, child: T, parentWidthMeasureSpec: Int,
                                widthUsed: Int, parentHeightMeasureSpec: Int,
                                heightUsed: Int): Boolean {
        widthMeasureSpec = parentWidthMeasureSpec
        this.widthUsed = widthUsed
        this.heightUsed = heightUsed
        layoutHeight = child.layoutParams?.height ?: 0
        if (sheetHeight == 0) sheetHeight = layoutHeight
        if (sheetHeight != layoutHeight) {
            (child.layoutParams as? CoordinatorLayout.LayoutParams)?.setSheet()
            parent?.onMeasureChild(child, parentWidthMeasureSpec, widthUsed,
                    View.MeasureSpec.makeMeasureSpec(layoutHeight, View.MeasureSpec.EXACTLY),
                    heightUsed)
            targetHeight = child.measuredHeight
            (child.layoutParams as? CoordinatorLayout.LayoutParams)?.setLayout()
            parent?.onMeasureChild(child, parentWidthMeasureSpec, widthUsed,
                    parentHeightMeasureSpec, heightUsed)
            initialHeight = child.measuredHeight
            return true
        }
        return false
    }

    override fun onLayoutChild(parent: CoordinatorLayout?, child: T?,
                               layoutDirection: Int): Boolean {
        (child?.layoutParams as? CoordinatorLayout.LayoutParams)?.setSheet()
        parent?.onLayoutChild(child, parent.layoutDirection)
        targetLeft = child?.left ?: 0
        targetTop = child?.top ?: 0
        (child?.layoutParams as? CoordinatorLayout.LayoutParams)?.setLayout()
        parent?.onLayoutChild(child, parent.layoutDirection)
        initialLeft = child?.left ?: 0
        initialTop = child?.top ?: 0
        return true
    }

    override fun onAttachedToLayoutParams(params: CoordinatorLayout.LayoutParams) {
        BottomSheetMasterCallback.callback.registerCallback(internalCallback)
    }

    override fun onDetachedFromLayoutParams() {
        BottomSheetMasterCallback.callback.unregisterCallback(internalCallback)
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: T?,
                                 dependency: View?): Boolean {
        val bottomSheetBehavior = (dependency?.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? BottomSheetBehavior
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setBottomSheetCallback(
                    BottomSheetMasterCallback.callback)
            return true
        }
        return false
    }

    private class Callback<T : View>(
            val bottomSheetDependentBehavior: BottomSheetDependentBehavior<T>) : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            bottomSheetDependentBehavior.bottomSheetOffset = slideOffset
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {

        }
    }
}

class BottomSheetMasterCallback private constructor() : BottomSheetBehavior.BottomSheetCallback() {

    companion object {
        val callback: BottomSheetMasterCallback = BottomSheetMasterCallback()
    }

    private val callbacks = ArrayList<BottomSheetBehavior.BottomSheetCallback>()

    fun registerCallback(callback: BottomSheetBehavior.BottomSheetCallback) = callbacks.add(
            callback)

    fun unregisterCallback(callback: BottomSheetBehavior.BottomSheetCallback) = callbacks.remove(
            callback)

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        callbacks.forEach { it.onSlide(bottomSheet, slideOffset) }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        callbacks.forEach { it.onStateChanged(bottomSheet, newState) }
    }
}

