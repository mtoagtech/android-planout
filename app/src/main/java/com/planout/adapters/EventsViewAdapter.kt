package com.planout.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.EventDetailsActivity
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.fragments.CompanyEventFragment
import com.planout.models.UpcomingEventData
import com.skydoves.balloon.*
import okhttp3.FormBody

class EventsViewAdapter(
    val activity: Activity?,
    val lifecycleOwner: LifecycleOwner,
    val showMenu: Boolean,
    val upcoming_eventsList: ArrayList<UpcomingEventData>,
    val act: CompanyEventFragment
) : RecyclerView.Adapter<EventsViewAdapter.ViewHolder>() {

    lateinit var layoutManager: LinearLayoutManager
    var deletedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_events_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item=upcoming_eventsList[position]
        Utility.SetImageSimple(activity!!,item.event_image,holder.imgEvent)
        holder.txtTitle.text=item.event_title
        holder.txtSubTitle.text=item.description
        holder.txtAddress.text=item.location.getString("address")
        val apistarttime=item.starttime
        val apiendtime=item.endtime
        val apievent_date=item.event_date

        val event_date=Utility.formatdateevent(apievent_date)
        val starttime=Utility.formattimeevent(apistarttime)
        val endtime=Utility.formattimeevent(apiendtime)


        holder.txtDateTime.text= "$event_date, $starttime - $endtime"

        if (showMenu){
            holder.imgMenu.visibility = View.VISIBLE
            if(Utility.getForm(activity, Utility.key.is_owner) == "1"){
                holder.imgMenu.setOnClickListener {
                    holder.imgMenu.showAlignLeft(showEditDeletePopUp(item.id,position,item))
                }
            }
            else{
                holder.imgMenu.isEnabled = false
            }

        }else{
            holder.imgMenu.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val ev_date= Utility.formatdateevent(apievent_date)
            val sttime= Utility.formattimeevent(apistarttime)
            val entime= Utility.formattimeevent(apiendtime)
            activity.startActivity(
                Intent(activity, EventDetailsActivity::class.java)
                .putExtra("eventImg", item.event_image)
                .putExtra("storeImg", item.store_image)
                .putExtra("eventName", item.event_title)
                .putExtra("eventDateTime", "$ev_date, $sttime - $entime")
                .putExtra("eventAddress", item.location.getString("address"))
                .putExtra("eventDesc", item.description))
        }
    }

    override fun getItemCount(): Int {
        return upcoming_eventsList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenu = itemView.findViewById<ImageView>(R.id.imgMenu)
        val imgEvent = itemView.findViewById<ImageView>(R.id.imgEvent)
        val txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        val txtDateTime = itemView.findViewById<TextView>(R.id.txtDateTime)
        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
        val txtSubTitle = itemView.findViewById<TextView>(R.id.txtSubTitle)
    }

    fun showEditDeletePopUp(id: String, position: Int, obj: UpcomingEventData) : Balloon {
        val balloon = Balloon.Builder(this.activity!!)
            .setHeight(BalloonSizeSpec.WRAP)
            .setWidth(BalloonSizeSpec.WRAP)
            .setLayout(R.layout.popup_edit_delete_view)
            .setArrowSize(10)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowOrientation(ArrowOrientation.END)
            .setArrowPosition(0.5f)
            .setCornerRadius(4f)
            .setBackgroundColor(ContextCompat.getColor(this.activity, R.color.background_color))
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
            .setLifecycleOwner(lifecycleOwner)
            .build()

        val txtDefault = balloon.getContentView().findViewById(R.id.txtDefault) as TextView
        val view1 = balloon.getContentView().findViewById(R.id.view1) as View
        val txtEdit = balloon.getContentView().findViewById(R.id.txtEdit) as TextView
        val txtDelete = balloon.getContentView().findViewById(R.id.txtDelete) as TextView
        txtEdit.setOnClickListener {  balloon.dismiss()
            act.eventTitle = obj.event_title
            act.eventStartTime = Utility.formattimeevent(obj.starttime)!!.replace("am", "AM").replace("pm", "PM")
            act.eventEndTime = Utility.formattimeevent(obj.endtime)!!.replace("am", "AM").replace("pm", "PM")
            act.eventLocationId = obj.location_id
            act.eventLocation = obj.location.getString("address")
            act.eventImageUrl = obj.event_image
            act.eventDescription = obj.description
            act.eventId = obj.id
            act.eventStoreId = obj.store_id
            act.eventDateTime = obj.event_date
            act.locationListApi(true)
        }
        txtDefault.visibility = View.GONE
        view1.visibility = View.GONE
        txtDelete.setOnClickListener { balloon.dismiss()
            deletedPos = position
            //dialog for delete event confirmation
            openConfirmationPop(id, "Confirmation", "Are you sure you want to\ndelete this event?", "Confirm")
        }

        return balloon
    }

    fun openConfirmationPop(id: String, title: String, msg: String, btn: String){
        val dialog = Dialog(this.activity!!)
        dialog.setContentView(R.layout.logout_popup_view)
        val txtTitle = dialog.findViewById<TextView>(R.id.txtTitle)
        val txtSubTitle = dialog.findViewById<TextView>(R.id.txtSubTitle)
        val txtCancel = dialog.findViewById<TextView>(R.id.txtCancel)
        val txtDelete = dialog.findViewById<TextView>(R.id.txtDelete)

        txtTitle.text = title
        txtSubTitle.text = msg
        txtDelete.text = btn
        txtCancel.setOnClickListener {
            dialog.dismiss()
        }
        txtDelete.setOnClickListener {
            eventDeletApi(id)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun eventDeletApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            "${ApiController.api.events}/$id",
            activity!!,
            Utility.eventsDelete,
            true,
            Utility.DELETE,
            true
        )
    }
}