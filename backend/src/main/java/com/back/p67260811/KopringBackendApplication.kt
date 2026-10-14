package com.back.p67260811

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class KopringBackendApplication

fun main(args: Array<String>) {
    runApplication<KopringBackendApplication>(*args)
}