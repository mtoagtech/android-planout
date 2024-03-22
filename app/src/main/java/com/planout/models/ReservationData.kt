package com.planout.models

import org.json.JSONObject

/**
 * Created by Atul Papneja on 05-Jul-22.
 */
class ReservationData {
    lateinit var id:String
    lateinit var store_id:String
    lateinit var res_id:String
    lateinit var contact_name:String
    lateinit var contact_mobile:String
    lateinit var total_people:String
    lateinit var resdate:String
    lateinit var restime:String
    lateinit var store_name:String
    lateinit var name:String
    lateinit var status:String
    lateinit var location_id:String
    lateinit var preferred_table:String
    lateinit var extra_notes:String
    lateinit var store_image:String
    lateinit var remark:String
    lateinit var location:JSONObject
    lateinit var table_no:String
    lateinit var age_group:String
     var isEdit:Boolean = false
    var is_arrived:Int = 0
}