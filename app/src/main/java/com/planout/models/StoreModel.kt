package com.planout.models

import org.json.JSONObject

/**
 * Created by Atul Papneja on 04-Jul-22.
 */
class StoreModel{
    lateinit var id:String
    lateinit var store_name:String
    lateinit var store_image:String
    lateinit var cover_image:String
    lateinit var is_open:String
    lateinit var starttime:String
    lateinit var endtime:String
    lateinit var starttime1:String
    lateinit var endtime1:String
    lateinit var email:String
    lateinit var name:String
    lateinit var mobile:String
    lateinit var telephone:String
    lateinit var fax:String
    lateinit var default_location_id:String
    lateinit var is_favorite:String

    ////////arrays//////////
    val media: ArrayList<StoreMediaData> = ArrayList()
    val locations: ArrayList<StoreLocationData> = ArrayList()
//    timings
//    events
var tags: ArrayList<TagData> = ArrayList()
    var industries: ArrayList<IndustriesData> = ArrayList()

    //////////obj/////////
    lateinit var default_location:JSONObject

/*
    id
    store_name
    store_image
    cover_image
    is_open
    starttime
    endtime
    starttime1
    endtime1
    email
    name
    mobile
    telephone
    fax
    default_location_id
    media
    locations
    timings
    events
    tags
    industries
    default_location
*/
}


