package com.example.demo.address.controller

import com.example.demo.entity.Address
import com.example.demo.entity.District
import com.example.demo.entity.Province
import com.example.demo.entity.Ward
import com.example.demo.service.AddressService
import org.springframework.web.bind.annotation.*
import com.example.demo.address.dto.AddressRequest
import com.example.demo.address.dto.AddressResponse
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/addresses")
class AddressController(private val addressService: AddressService) {

    @GetMapping("/provinces")
    fun getProvinces(): List<Province> = addressService.getAllProvinces()

    @GetMapping("/districts/{provinceId}")
    fun getDistricts(@PathVariable provinceId: Int): List<District> =
        addressService.getDistrictsByProvince(provinceId)

    @GetMapping("/wards/{districtId}")
    fun getWards(@PathVariable districtId: Int): List<Ward> =
        addressService.getWardsByDistrict(districtId)

    @PostMapping("/fetch/provinces")
    fun fetchProvinces() {
        addressService.fetchAndSaveProvinces()
    }
    
    @PostMapping("/fetch/districts")
    fun fetchDistricts() {
        addressService.fetchAndSaveDistricts()
    }
    
    @PostMapping("/fetch/wards")
    fun fetchWards() {
        addressService.fetchAndSaveWards()
    }

    @PostMapping("/add")
    fun addAddress(@RequestBody request: AddressRequest): AddressResponse {
        println("🔍 Controller received request for userId=${request.userId}")
        return addressService.addAddress(
            userId = request.userId,
            provinceId = request.provinceId,
            districtId = request.districtId,
            wardId = request.wardId,
            street = request.street,
            isDefault = request.isDefault
        )
    }

    // 🔥 API Lấy Địa Chỉ Cụ Thể
    @GetMapping("/{addressId}")
    fun getAddressById(@PathVariable addressId: UUID): AddressResponse {
        return addressService.getAddressById(addressId)
    }

    // 🔥 API Cập Nhật Địa Chỉ
    @PutMapping("/{addressId}")
    fun updateAddress(@PathVariable addressId: UUID, @RequestBody request: AddressRequest): AddressResponse {
        return addressService.updateAddress(addressId, request)
    }

    // 🔥 API Xóa Địa Chỉ
    @DeleteMapping("/{addressId}")
    fun deleteAddress(@PathVariable addressId: UUID) {
        addressService.deleteAddress(addressId)
    }

    // 🔥 API Đặt Địa Chỉ Mặc Định
    @PutMapping("/{addressId}/set-default")
    fun setDefault(@PathVariable addressId: UUID) {
        addressService.setDefaultAddress(addressId)
    }

    @GetMapping("/user/{userId}")
    fun getUserAddresses(
        @PathVariable userId: Int,
        @PageableDefault(size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<Page<AddressResponse>> {
        val addresses = addressService.getUserAddresses(userId, pageable)
        return ResponseEntity.ok(addresses)
    }
}