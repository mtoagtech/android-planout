package com.planout.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.planout.R
import com.planout.adapters.CarouselAdapter
import com.planout.adapters.VisitorPhotosViewAdapter
import com.planout.constant.Utility
import com.planout.models.StoreMediaData
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.activity_visitor_photo_gallery.*
import java.lang.reflect.Type

class VisitorPhotoGalleryActivity : AppCompatActivity() {
    var isFrom = ""
    private val photosurllist: ArrayList<String> = ArrayList()
    var scrollPos=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_photo_gallery)
        imgBackHeader.setOnClickListener {
            onBackPressed()
        }
        isFrom=intent.getStringExtra(Utility.key.isFrom)!!
        val listPrivate: ArrayList<StoreMediaData>
        val type: Type = object : TypeToken<List<StoreMediaData?>?>() {}.type
        listPrivate = Gson().fromJson(intent.getStringExtra("private_list"), type)

        for (i in 0 until listPrivate.size) {
            if (i==intent.getStringExtra(Utility.key.itemposition)!!.toInt()){
                scrollPos=i
            }
            photosurllist.add(listPrivate[i].media_url)
        }
        /////////RecyclerView//////////////////
//        imagesRecy.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val photoAdapter = VisitorPhotosViewAdapter(this, listPrivate,carouselView)
        imagesRecy.layoutManager = layoutManager
        imagesRecy.adapter = photoAdapter
        imagesRecy.scrollToPosition(scrollPos)

        /////////CarouselView////////////////
        val carouselAdapter= CarouselAdapter(this,photosurllist)
        carouselView.adapter=carouselAdapter
        carouselView.scrollToPosition(scrollPos)
        carouselView.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.CENTER) // CENTER is a default one
                .build()
        )
        carouselView.setItemTransitionTimeMillis(300)
        carouselView.setSlideOnFling(true)
        carouselView.setItemTransitionTimeMillis(300)
        carouselView.addOnItemChangedListener { viewHolder, adapterPosition ->
            for (i in 0 until listPrivate.size){
                listPrivate[i].isUrl = i == adapterPosition
            }
            imagesRecy.scrollToPosition(adapterPosition)
            photoAdapter.notifyDataSetChanged()
        }
    }
}