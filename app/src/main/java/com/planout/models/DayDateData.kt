package com.planout.models

class DayDateData {

    private lateinit var Day: String
    private lateinit var Date: String
    private lateinit var Full_Date: String
    private var Clicked: Boolean = false
    private var isAvailable: Boolean = false

    fun getisAvailable(): Boolean {
        return isAvailable
    }

    fun setisAvailable(id: Boolean) {
        isAvailable = id
    }

    fun getClicked(): Boolean {
        return Clicked
    }

    fun setClicked(id: Boolean) {
        Clicked = id
    }

    fun getDay(): String {
        return Day
    }

    fun setDay(id: String) {
        Day = id
    }


    fun getDate(): String {
        return Date
    }

    fun setDate(name: String) {
        Date = name
    }

    fun getFull_Date(): String {
        return Full_Date
    }

    fun setFull_Date(id: String) {
        Full_Date = id
    }
}