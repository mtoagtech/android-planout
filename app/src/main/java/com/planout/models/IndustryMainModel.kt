package com.planout.models

data class IndustryMainModel(

	var data: List<IndustryModel?>? = null,

	var success: Boolean? = null,

	var message: String? = null
)

data class IndustryModel(

	var industryName: String? = null,

	var industryImage: String? = null,

	var id: String? = null,

	var isSelected: Boolean? = false
)
