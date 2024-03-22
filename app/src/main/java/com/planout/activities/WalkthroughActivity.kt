package com.planout.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.planout.R
import com.planout.constant.Utility
import com.planout.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_walkthrough.*
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class WalkthroughActivity : AppCompatActivity() {

    var sliderDotspanel: LinearLayout? = null
    private var dotscount = 0
    private lateinit var dots: Array<ImageView>
    lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walkthrough)
        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)*/
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT

        viewPagerAdapter = ViewPagerAdapter(this)

        viewPager!!.adapter = viewPagerAdapter

        dotscount = viewPagerAdapter.getCount()
        val dots = arrayOfNulls<ImageView>(dotscount)

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0){
                    setView(view1, view2, view3)
                    txtTitle.text = getString(R.string.discover_delicious)
                }else if (position == 1){
                    setView(view2, view1, view3)
                    txtTitle.text = getString(R.string.reserve_table)
                }else if (position == 2){
                    setView(view3, view2, view1)
                    txtTitle.text = getString(R.string.instant_notification)
                }else{
                    setView(view1, view2, view3)
                    txtTitle.text = getString(R.string.discover_delicious)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        clickView()
    }

    private fun clickView() {
        txtBtnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        txtBtnSignup.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
        Utility.animationClick(txtGuest).setOnClickListener {
            startActivity(Intent(this, HomeVisitorActivity::class.java))
        }
    }

    private fun setView(view1: View, view2: View, view3: View) {
        view1.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        view2.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_3D)
        view3.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray_3D)
    }
}