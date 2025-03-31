package com.example.demo.address.repository

import com.example.demo.entity.Address
import com.example.demo.entity.District
import com.example.demo.entity.Province
import com.example.demo.entity.Ward
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock // Correct import for @Lock
import org.springframework.data.jpa.repository.Query
import jakarta.persistence.LockModeType // Use jakarta.persistence for modern JPA (or javax.persistence for older versions)
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProvinceRepository : JpaRepository<Province, Int>

interface DistrictRepository : JpaRepository<District, Int> {
    fun findByProvinceId(provinceId: Int): List<District>
}

interface WardRepository : JpaRepository<Ward, Int> {
    fun findByDistrictId(districtId: Int): List<Ward>
}

interface AddressRepository : JpaRepository<Address, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // Correct annotation
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId")
    fun findByUserIdWithLock(userId: Int): List<Address>

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId")
    fun findByUserId(userId: Int): List<Address>

    fun findByUserId(userId: Int, pageable: Pageable): Page<Address> // New method
}