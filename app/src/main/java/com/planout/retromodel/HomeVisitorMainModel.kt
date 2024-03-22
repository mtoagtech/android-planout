package com.planout.retromodel

import com.google.gson.annotations.SerializedName

data class HomeVisitorMainModel(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class UpcomingEventsItem(

	@field:SerializedName("store_id")
	val storeId: Int? = null,

	@field:SerializedName("event_date")
	val eventDate: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("event_image")
	val eventImage: String? = null,

	@field:SerializedName("endtime")
	val endtime: String? = null,

	@field:SerializedName("event_title")
	val eventTitle: String? = null,

	@field:SerializedName("location")
	val location: Location? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("starttime")
	val starttime: String? = null,

	@field:SerializedName("location_id")
	val locationId: Int? = null,

	@field:SerializedName("store_image")
	val storeImage: String? = null
)

data class TagsItem(

	@field:SerializedName("tag_name")
	val tagName: String? = null,

	@field:SerializedName("pivot")
	val pivot: Pivot? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Location(

	@field:SerializedName("store_id")
	val storeId: Int? = null,

	@field:SerializedName("area")
	val area: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("address1")
	val address1: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("table_indoor")
	val tableIndoor: Int? = null,

	@field:SerializedName("table_outdoor")
	val tableOutdoor: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("postal_code")
	val postalCode: Int? = null,

	@field:SerializedName("city_id")
	val cityId: Int? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class Ios(

	@field:SerializedName("app_popup")
	val appPopup: String? = null
)

data class Pivot(

	@field:SerializedName("store_id")
	val storeId: Int? = null,

	@field:SerializedName("tag_id")
	val tagId: Int? = null,

	@field:SerializedName("industry_id")
	val industryId: Int? = null
)

data class RecordsItem(

	@field:SerializedName("is_favorite")
	val isFavorite: Boolean? = null,

	@field:SerializedName("default_location")
	val defaultLocation: DefaultLocation? = null,

	@field:SerializedName("is_open")
	val isOpen: Int? = null,

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("endtime")
	val endtime: String? = null,

	@field:SerializedName("telephone")
	val telephone: String? = null,

	@field:SerializedName("package_end_date")
	val packageEndDate: String? = null,

	@field:SerializedName("starttime")
	val starttime: String? = null,

	@field:SerializedName("package_id")
	val packageId: Int? = null,

	@field:SerializedName("tags")
	val tags: List<TagsItem?>? = null,

	@field:SerializedName("endtime1")
	val endtime1: String? = null,

	@field:SerializedName("industries")
	val industries: List<IndustriesItem?>? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("store_name")
	val storeName: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("cover_image")
	val coverImage: String? = null,

	@field:SerializedName("starttime1")
	val starttime1: String? = null,

	@field:SerializedName("fax")
	val fax: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("store_image")
	val storeImage: String? = null
)

data class Android(

	@field:SerializedName("app_version")
	val appVersion: String? = null,

	@field:SerializedName("force_update")
	val forceUpdate: String? = null
)

data class Data(

	@field:SerializedName("total")
	val total: Int? = null,

	@field:SerializedName("records")
	val records: List<RecordsItem?>? = null,

	@field:SerializedName("upcoming_events")
	val upcomingEvents: List<UpcomingEventsItem?>? = null,

	@field:SerializedName("featured_stores")
	val featuredStores: List<Any?>? = null,

	@field:SerializedName("app_params")
	val appParams: AppParams? = null,

	@field:SerializedName("popular_industries")
	val popularIndustries: List<PopularIndustriesItem?>? = null,

	@field:SerializedName("total_unread_notifications")
	val totalUnreadNotifications: Int? = null,

	@field:SerializedName("current_page")
	val currentPage: Int? = null
)

data class AppParams(

	@field:SerializedName("android")
	val android: Android? = null,

	@field:SerializedName("ios")
	val ios: Ios? = null
)

data class PopularIndustriesItem(

	@field:SerializedName("industry_name")
	val industryName: String? = null,

	@field:SerializedName("stores")
	val stores: List<StoresItem?>? = null,

	@field:SerializedName("industry_image")
	val industryImage: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class StoresItem(

	@field:SerializedName("is_favorite")
	val isFavorite: Boolean? = null,

	@field:SerializedName("default_location")
	val defaultLocation: DefaultLocation? = null,

	@field:SerializedName("is_open")
	val isOpen: Int? = null,

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("endtime")
	val endtime: String? = null,

	@field:SerializedName("telephone")
	val telephone: String? = null,

	@field:SerializedName("package_end_date")
	val packageEndDate: String? = null,

	@field:SerializedName("starttime")
	val starttime: String? = null,

	@field:SerializedName("package_id")
	val packageId: Int? = null,

	@field:SerializedName("tags")
	val tags: List<TagsItem?>? = null,

	@field:SerializedName("endtime1")
	val endtime1: String? = null,

	@field:SerializedName("industries")
	val industries: List<IndustriesItem?>? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("store_name")
	val storeName: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("cover_image")
	val coverImage: Any? = null,

	@field:SerializedName("starttime1")
	val starttime1: String? = null,

	@field:SerializedName("fax")
	val fax: Any? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("store_image")
	val storeImage: Any? = null
)

data class DefaultLocation(

	@field:SerializedName("store_id")
	val storeId: Int? = null,

	@field:SerializedName("area")
	val area: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("address1")
	val address1: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("table_indoor")
	val tableIndoor: Int? = null,

	@field:SerializedName("table_outdoor")
	val tableOutdoor: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("postal_code")
	val postalCode: Int? = null,

	@field:SerializedName("city_id")
	val cityId: Int? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class IndustriesItem(

	@field:SerializedName("industry_name")
	val industryName: String? = null,

	@field:SerializedName("pivot")
	val pivot: Pivot? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
