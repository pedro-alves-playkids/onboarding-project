package com.playkids.onboarding.api.route

import com.movile.kotlin.commons.ktor.patch
import com.movile.kotlin.commons.ktor.post
import com.playkids.onboarding.api.dto.AddItemDTO
import com.playkids.onboarding.core.model.Profile
import com.playkids.onboarding.core.service.ProfileService
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

fun Route.profileRouting(profileService: ProfileService) {

    fun PipelineContext<*, ApplicationCall>.id(): String {
        return call.parameters["id"] ?: throw IllegalArgumentException("an id must be provided")
    }

    route("/profile") {
        get("{id}") {
            val id = id()

            val profile = profileService.find(id) ?: throw NotFoundException("id doesn't exists")

            call.respond(profile)
        }
        patch<AddItemDTO>("{id}") {item ->
            val id = id()

            profileService.addItem(id, item.itemId)

            call.respondText("Item added to profile", status = HttpStatusCode.OK)
        }
        post<Profile> { profile ->
            profileService.create(profile)

            call.respondText("Profile Created", status = HttpStatusCode.OK)
        }
    }
}