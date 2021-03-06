package com.playkids.onboarding.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.playkids.onboarding.api.extensions.exceptions
import com.playkids.onboarding.api.extensions.logger
import com.playkids.onboarding.api.route.itemRouting
import com.playkids.onboarding.core.service.ItemService
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class OnboardingApi(
    private val serverPort: Int,
    private val itemService: ItemService
){
    fun start() {
        embeddedServer(Netty, serverPort) {

            install(StatusPages) {
                exceptions(logger)
            }

            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                }
            }

            routing {
                get("/test") {
                    call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
                }
                route("/api") {
                    itemRouting(itemService)
                }

            }
        }.start(wait = true)
    }

    companion object {
        internal val logger = logger<OnboardingApi>()
    }
}