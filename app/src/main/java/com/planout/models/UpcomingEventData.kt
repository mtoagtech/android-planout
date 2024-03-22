package com.planout.models

import org.json.JSONObject

/**
 * Created by Atul Papneja on 04-Jul-22.
 */
class UpcomingEventData {
    lateinit var id:String
    lateinit var store_id:String
    lateinit var store_image:String
    lateinit var location_id:String
    lateinit var event_title:String
    lateinit var description:String
    lateinit var event_date:String
    lateinit var starttime:String
    lateinit var endtime:String
    lateinit var event_image:String
    lateinit var location:JSONObject
}