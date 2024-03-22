package com.planout.models

/**
 * Created by Atul Papneja on 13-Jul-22.
 */
class PackagesData {
    lateinit var id:String
    lateinit var package_name:String
    lateinit var duration:String
    lateinit var price:String
    lateinit var tax_percent:String
    var available_vouchers: ArrayList<VouchersData> = ArrayList()
    var isActive=false
    var isSelected=false


}