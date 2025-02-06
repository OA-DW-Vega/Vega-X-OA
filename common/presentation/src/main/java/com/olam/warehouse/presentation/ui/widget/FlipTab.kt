package com.olam.warehouse.presentation.ui.widget

/**
 * Created by Baskaran Kannan on 2/25/2020.
 */
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.databinding.FliptabBinding


class FlipTab : FrameLayout {
    private lateinit var binding: FliptabBinding
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(attrs)
    }

    private var isLeftSelected: Boolean = true
    private var animationMiddleViewFlippedFlag: Boolean = false

    private val leftTabText get() = binding.tabLeft.text.toString()
    private val rightTabText get() = binding.tabRight.text.toString()

    private var tabSelectedListener: TabSelectedListener? = null

    private val leftSelectedDrawable by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(R.drawable.tab_left_selected, null)
        } else {
            resources.getDrawable(R.drawable.tab_left_selected)
        }
    }
    private val rightSelectedDrawable by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(R.drawable.tab_right_selected, null)
        } else {
            resources.getDrawable(R.drawable.tab_right_selected)
        }
    }

    companion object {
        private val FLIP_ANIMATION_DURATION = 500
        private val WOBBLE_RETURN_ANIMATION_DURATION = 250
        private val WOBBLE_ANGLE: Float = 5f
        private val OVERALL_COLOR: Int = Color.parseColor("#ff0099cc")
    }

    private var flipAnimationDuration = FLIP_ANIMATION_DURATION
    private var wobbleReturnAnimationDuration = WOBBLE_RETURN_ANIMATION_DURATION
    private var wobbleAngle = WOBBLE_ANGLE


    init {
        val view = inflate(context, R.layout.fliptab, this)
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FliptabBinding.bind(view)
        binding.tabLeft.setOnClickListener {
            if (isLeftSelected) {
                tabSelectedListener?.onTabReselected(isLeftSelected, leftTabText)
            } else {
                flipTabs()
            }
        }
        binding.tabRight.setOnClickListener {
            if (isLeftSelected) {
                flipTabs()
            } else {
                tabSelectedListener?.onTabReselected(isLeftSelected, rightTabText)
            }
        }
        clipChildren = false
        clipToPadding = false
    }

    private fun initialize(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FlipTab, 0, 0)
            typedArray.apply {
                flipAnimationDuration =
                    getInt(R.styleable.FlipTab_flipAnimationDuration, FLIP_ANIMATION_DURATION)
                wobbleReturnAnimationDuration = getInt(
                    R.styleable.FlipTab_wobbleReturnAnimationDuration,
                    WOBBLE_RETURN_ANIMATION_DURATION
                )
                wobbleAngle = getFloat(R.styleable.FlipTab_wobbleAngle, WOBBLE_ANGLE)
                if (hasValue(R.styleable.FlipTab_overallColor)) {
                    setOverallColor(getColor(R.styleable.FlipTab_overallColor, OVERALL_COLOR))
                } else {
                    setTextColor(getColor(R.styleable.FlipTab_textColor, OVERALL_COLOR))
                    setHighlightColor(getColor(R.styleable.FlipTab_highlightColor, OVERALL_COLOR))
                }
                if (typedArray.getInt(R.styleable.FlipTab_startingTab, 0) == 1) {
                    isLeftSelected = false
                    binding.tabSelectedContainer.rotationY = 180f
                    binding.tabSelected.background = rightSelectedDrawable
                    binding.tabSelected.scaleX = -1f
                }
                setLeftTabText(getString(R.styleable.FlipTab_leftTabText) ?: "Left tab")
                setRightTabText(getString(R.styleable.FlipTab_rightTabText) ?: "Right tab")
            }
            typedArray.recycle()
        }
    }

    fun flipTabs() {
        animationMiddleViewFlippedFlag = false
        isLeftSelected = !isLeftSelected
        binding.tabSelectedContainer.animate()
            .rotationY(if (isLeftSelected) 0f else 180f)
            .setDuration(flipAnimationDuration.toLong())
            .withStartAction {
                (parent as ViewGroup?)?.clipChildren = false
                (parent as ViewGroup?)?.clipToPadding = false
                tabSelectedListener?.onTabSelected(
                    isLeftSelected,
                    if (isLeftSelected) leftTabText else rightTabText
                )
            }
            .setUpdateListener {
                if (animationMiddleViewFlippedFlag) return@setUpdateListener

                //TODO: Find out a better alternative to changing Background in the middle of animation (might result in dropped frame/stutter)
                if (isLeftSelected && binding.tabSelectedContainer.rotationY <= 90f) {
                    animationMiddleViewFlippedFlag = true
                    binding.tabSelected.text = leftTabText
                    binding.tabSelected.background = leftSelectedDrawable
                    binding.tabSelected.scaleX = 1f
                } else if (!isLeftSelected && binding.tabSelectedContainer.rotationY >= 90f) {
                    animationMiddleViewFlippedFlag = true
                    binding.tabSelected.text = rightTabText
                    binding.tabSelected.background = rightSelectedDrawable
                    binding.tabSelected.scaleX = -1f
                }
            }
            .withEndAction {
                (parent as ViewGroup?)?.clipChildren = true
                (parent as ViewGroup?)?.clipToPadding = true
            }
            .start()
        val animSet = AnimatorSet()
        val animator1 = ObjectAnimator.ofFloat(
            binding.baseFliptabContainer,
            "rotationY",
            if (isLeftSelected) -wobbleAngle else wobbleAngle
        )
        animator1.duration = flipAnimationDuration.toLong()
        val animator2 = ObjectAnimator.ofFloat(binding.baseFliptabContainer, "rotationY", 0f)
        animator2.duration = wobbleReturnAnimationDuration.toLong()
        animSet.playSequentially(animator1, animator2)
        animSet.start()
    }

    interface TabSelectedListener {
        fun onTabSelected(isLeftTab: Boolean, tabTextValue: String): Unit
        fun onTabReselected(isLeftTab: Boolean, tabTextValue: String): Unit
    }

    fun setTabSelectedListener(tabSelectedListener: TabSelectedListener) {
        this.tabSelectedListener = tabSelectedListener
    }

    fun setWobbleAngle(angle: Float) {
        wobbleAngle = angle
    }

    fun setWobbleReturnAnimationDuration(duration: Int) {
        wobbleReturnAnimationDuration = duration
    }

    fun setFlipAnimationDuration(duration: Int) {
        flipAnimationDuration = duration
    }

    fun setOverallColor(color: Int) {
        setTextColor(ContextCompat.getColor(context, R.color.mid_grey))
        setHighlightColor(color)
    }

    fun setTextColor(color: Int) {
        binding.tabLeft.setTextColor(color)
        binding.tabRight.setTextColor(color)
    }

    fun setHighlightColor(color: Int) {
        (binding.tabLeft.background as GradientDrawable).setStroke(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                resources.displayMetrics
            ).toInt(), color
        )
        (binding.tabRight.background as GradientDrawable).setStroke(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                resources.displayMetrics
            ).toInt(), color
        )
        (binding.tabSelected.background as GradientDrawable).setStroke(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                0f,
                resources.displayMetrics
            ).toInt(), color
        )
        (binding.tabSelected.background as GradientDrawable).setColor(color)
        DrawableCompat.setTint(leftSelectedDrawable, color)
        DrawableCompat.setTint(rightSelectedDrawable, color)
    }

    fun setLeftTabText(text: String) {
        binding.tabLeft.text = text
        if (isLeftSelected) {
            binding.tabSelected.text = text
        }
    }

    fun setRightTabText(text: String) {
        binding.tabRight.text = text
        if (!isLeftSelected) {
            binding.tabSelected.text = text
        }
    }

    fun selectLeftTab(withAnimation: Boolean) {
        if (!isLeftSelected) {
            if (withAnimation) {
                flipTabs()
            } else {
                isLeftSelected = true
                binding.tabSelectedContainer.rotationY = 0f
                binding.tabSelected.text = leftTabText
                binding.tabSelected.background = leftSelectedDrawable
                binding.tabSelected.scaleX = 1f
                tabSelectedListener?.onTabSelected(isLeftSelected, leftTabText)
            }
        } else {
            tabSelectedListener?.onTabReselected(isLeftSelected, leftTabText)
        }
    }

    fun selectRightTab(withAnimation: Boolean) {
        if (isLeftSelected) {
            if (withAnimation) {
                flipTabs()
            } else {
                isLeftSelected = false
                binding.tabSelectedContainer.rotationY = 180f
                binding.tabSelected.text = rightTabText
                binding.tabSelected.background = rightSelectedDrawable
                binding.tabSelected.scaleX = -1f
                tabSelectedListener?.onTabSelected(isLeftSelected, rightTabText)
            }
        } else {
            tabSelectedListener?.onTabReselected(isLeftSelected, rightTabText)
        }
    }
}
