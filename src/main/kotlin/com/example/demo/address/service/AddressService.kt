package com.example.demo.service

import com.example.demo.entity.District
import com.example.demo.entity.Province
import com.example.demo.entity.Ward
import com.example.demo.repository.*
import com.example.demo.entity.Users
import com.example.demo.entity.Address
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.example.demo.address.dto.ProvinceDTO
import com.example.demo.address.dto.DistrictDTO
import com.example.demo.address.dto.WardDTO
import com.example.demo.address.dto.ProvinceDetailDTO
import com.example.demo.address.dto.DistrictDetailDTO
import com.example.demo.address.repository.*
import java.util.*
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.slf4j.LoggerFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import com.example.demo.address.dto.AddressRequest
import com.example.demo.address.dto.AddressResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class AddressService(
    private val provinceRepository: ProvinceRepository,
    private val districtRepository: DistrictRepository,
    private val wardRepository: WardRepository,
    private val addressRepository: AddressRepository, // Thêm dòng này
    private val userRepository: UserRepository // Thêm dòng này
) {
    private val webClient = WebClient.builder().baseUrl("https://provinces.open-api.vn/api/").build()
    private val logger = LoggerFactory.getLogger(AddressService::class.java)
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    // Lấy danh sách tỉnh/thành và lưu vào DB
    @Transactional
    fun fetchAndSaveProvinces() {
        val provinces = webClient.get()
            .uri("p/")
            .retrieve()
            .bodyToFlux(ProvinceDTO::class.java)
            .collectList()
            .block() ?: emptyList()

        val provinceEntities = provinces.map { Province(id = it.code.toInt(), name = it.name) }
        provinceRepository.saveAll(provinceEntities)
    }

    // Lấy danh sách quận/huyện theo tỉnh và lưu vào DB
    @Transactional
    fun fetchAndSaveDistricts() {
        val provinces = provinceRepository.findAll()
        provinces.forEach { province ->
            val districts = webClient.get()
                .uri("p/${province.id}?depth=2") // Sử dụng id thay vì code
                .retrieve()
                .bodyToMono(ProvinceDetailDTO::class.java)
                .block()?.districts ?: emptyList()

            val districtEntities = districts.map { 
                District(id = it.code.toInt(), name = it.name, province = province) 
            }
            districtRepository.saveAll(districtEntities)
        }
    }

    // Lấy danh sách phường/xã theo quận và lưu vào DB
    @Transactional
    fun fetchAndSaveWards() {
        val districts = districtRepository.findAll()
        districts.forEach { district ->
            val wards = webClient.get()
                .uri("d/${district.id}?depth=2") // Sử dụng id thay vì code
                .retrieve()
                .bodyToMono(DistrictDetailDTO::class.java)
                .block()?.wards ?: emptyList()

            val wardEntities = wards.map { 
                Ward(id = it.code.toInt(), name = it.name, district = district) 
            }
            wardRepository.saveAll(wardEntities)
        }
    }

    fun getAllProvinces(): List<Province> = provinceRepository.findAll()

    fun getDistrictsByProvince(provinceId: Int): List<District> =
    districtRepository.findByProvinceId(provinceId) // Đã khớp với repository

    fun getWardsByDistrict(districtId: Int): List<Ward> =
    wardRepository.findByDistrictId(districtId) // Đã khớp với repository



    @Transactional
    fun addAddress(
        userId: Int,
        provinceId: Int,
        districtId: Int,
        wardId: Int,
        street: String,
        isDefault: Boolean
    ): AddressResponse { // Changed return type to AddressResponse
        logger.info("🔍 [{}] Starting transaction addAddress() for userId={}", Thread.currentThread().name, userId)

        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        val province = provinceRepository.findById(provinceId).orElseThrow { throw RuntimeException("Province not found") }
        val district = districtRepository.findById(districtId).orElseThrow { throw RuntimeException("District not found") }
        val ward = wardRepository.findById(wardId).orElseThrow { throw RuntimeException("Ward not found") }

        val addresses = addressRepository.findByUserId(userId)
        logger.info("🔍 Retrieved {} addresses for userId={}", addresses.size, userId)
    addresses.forEach { 
        logger.info("🔍 Address ID={} street={} isDefault={}", it.id, it.street, it.isDefault)
    }

        val existingAddress = addresses.firstOrNull { 
            it.street == street && it.province.id == provinceId && it.district.id == districtId && it.ward.id == wardId
        }
        if (existingAddress != null) {
            throw RuntimeException("Address already exists")
        }

        val newAddress = Address(
            street = street,
            province = province,
            district = district,
            ward = ward,
            user = user,
            isDefault = isDefault
        )
        logger.info("🔍 Created new address with ID={}", newAddress.id ?: "to be generated")

        // Update existing addresses only if the new address is default and after ensuring it's new
        if (isDefault) {
            val addressesToUpdate = addresses.filter { it.isDefault } // Only update those that are true
            if (addressesToUpdate.isNotEmpty()) {
                addressesToUpdate.forEach { it.isDefault = false }
                addressRepository.saveAll(addressesToUpdate)
                entityManager.flush() // Force updates to DB
                logger.info("🔍 [{}] Updated {} existing default addresses to false", Thread.currentThread().name, addressesToUpdate.size)
            }
        }

        entityManager.clear() // Clear context to ensure a fresh insert
        val savedAddress = addressRepository.save(newAddress)
        
        logger.info("✅ [{}] Completing transaction addAddress() for userId={}", Thread.currentThread().name, userId)
        
        // Return AddressResponse instead of Address
        return AddressResponse(
            id = savedAddress.id,
            street = savedAddress.street,
            provinceId = savedAddress.province.id,
            districtId = savedAddress.district.id,
            wardId = savedAddress.ward.id,
            userId = savedAddress.user.id!!, // Safe assertion: user is persisted
            isDefault = savedAddress.isDefault
        )
    }

    fun getAddressById(addressId: UUID): AddressResponse {
        val address = addressRepository.findById(addressId)
            .orElseThrow { throw IllegalArgumentException("Address does not exist!") }

        return AddressResponse(
            id = address.id,
            street = address.street,
            provinceId = address.province.id,
            districtId = address.district.id,
            wardId = address.ward.id,
            userId = address.user.id!!,
            isDefault = address.isDefault
        )
    }

    @Transactional
    fun updateAddress(addressId: UUID, request: AddressRequest): AddressResponse {
        val address = addressRepository.findById(addressId)
            .orElseThrow { throw IllegalArgumentException("Address does not exist!") }

            val updatedAddress = address.copy(
                street = request.street,
                province = provinceRepository.findById(request.provinceId)
                    .orElseThrow { throw IllegalArgumentException("The province/city does not exist!") },
                district = districtRepository.findById(request.districtId)
                    .orElseThrow { throw IllegalArgumentException("District does not exist!") },
                ward = wardRepository.findById(request.wardId)
                    .orElseThrow { throw IllegalArgumentException("Ward/Commune does not exist!") },
                isDefault = request.isDefault
            )

        addressRepository.save(updatedAddress)

        return AddressResponse(
            id = updatedAddress.id,
            street = updatedAddress.street,
            provinceId = updatedAddress.province.id,
            districtId = updatedAddress.district.id,
            wardId = updatedAddress.ward.id,
            userId = updatedAddress.user.id!!,
            isDefault = updatedAddress.isDefault
        )
    }

    @Transactional
    fun deleteAddress(addressId: UUID) {
        val address = addressRepository.findById(addressId)
            .orElseThrow { throw IllegalArgumentException("Address does not exist!") }
        addressRepository.delete(address)
    }

    @Transactional
    fun setDefaultAddress(addressId: UUID) {
        val address = addressRepository.findById(addressId)
            .orElseThrow { throw IllegalArgumentException("Address does not exist!") }

        // Lấy user từ địa chỉ
        val user = address.user 

        // Bỏ default cho các địa chỉ cũ của user
        val addresses = addressRepository.findByUserIdWithLock(user.id!!)
        addresses.forEach { it.isDefault = false }
        addressRepository.saveAll(addresses)

        // Cập nhật địa chỉ mới thành mặc định
        address.isDefault = true
        addressRepository.save(address)
    }

    fun getUserAddresses(userId: Int, pageable: Pageable): Page<AddressResponse> {
        return addressRepository.findByUserId(userId, pageable).map { address ->
            AddressResponse(
                id = address.id,
                street = address.street,
                provinceId = address.province.id,
                districtId = address.district.id,
                wardId = address.ward.id,
                userId = address.user.id!!,
                isDefault = address.isDefault
            )
        }
    }
}
