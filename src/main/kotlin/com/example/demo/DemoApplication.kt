package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan("com.example.demo.entity")
@EnableJpaRepositories("com.example.demo.repository")
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}