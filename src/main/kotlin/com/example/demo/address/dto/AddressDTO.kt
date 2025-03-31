package com.example.demo.address.dto

import java.util.UUID

data class ProvinceDTO(
    val code: Int,
    val name: String
)

data class DistrictDTO(
    val code: Int,
    val name: String
)

data class WardDTO(
    val code: Int,
    val name: String
)

data class ProvinceDetailDTO(
    val districts: List<DistrictDTO>
)

data class DistrictDetailDTO(
    val wards: List<WardDTO>
)

data class AddressRequest(
    val userId: Int,
    val provinceId: Int,
    val districtId: Int,
    val wardId: Int,
    val street: String,
    val isDefault: Boolean
)

data class AddressResponse(
    val id: UUID?,
    val street: String,
    val provinceId: Int,
    val districtId: Int,
    val wardId: Int,
    val userId: Int,
    val isDefault: Boolean
)

