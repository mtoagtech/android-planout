package com.planout.models

/**
 * Created by Atul Papneja on 20-Jun-22.
 */
class StoreLocationData {
    lateinit var id:String
    lateinit var store_id:String
    lateinit var city_id:String
    lateinit var city_name:String
    lateinit var area:String
    lateinit var address:String
    lateinit var address1:String
    lateinit var postal_code:String
    lateinit var latitude:String
    lateinit var longitude:String
    lateinit var table_indoor:String
    lateinit var table_outdoor:String
    lateinit var locationObj:String
    lateinit var address_type:String
    var is_default:Boolean=false
    var isSelected:Boolean=false

}