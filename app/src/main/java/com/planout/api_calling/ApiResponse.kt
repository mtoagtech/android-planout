package com.planout.api_calling

import org.json.JSONObject

interface ApiResponse {

    fun onTaskComplete(result: JSONObject, type: String, isData: Boolean)

}