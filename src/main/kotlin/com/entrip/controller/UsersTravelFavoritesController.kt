package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.entity.TravelFavorite
import com.entrip.domain.entity.UsersTravelFavorites
import com.entrip.repository.UsersTravelFavoriteRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset

@RestController
class UsersTravelFavoritesController(
    private final val usersTravelFavoriteRepository: UsersTravelFavoriteRepository,
    private final val objectMapper: ObjectMapper
) {

    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<RestAPIMessages> {
        val restAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

    @PostMapping("/api/v2/usersTravelFavorite/{user_id}")
    fun addUsersTravelFavorite(
        @PathVariable user_id: String,
        @RequestBody travelFavorite: TravelFavorite
    ): ResponseEntity<RestAPIMessages> {
        // user_id에 대해서 만약 usersTravelFavorites 정보가 존재하지 않는 경우, usersTravelFavorite를 새롭게 저장
        if (!usersTravelFavoriteRepository.existsById(user_id)) saveUsersTravelFavorite(user_id)

        val target = usersTravelFavoriteRepository.findById(user_id).get()
        target.addTravelFavorite(travelFavorite)
        usersTravelFavoriteRepository.save(target)
        val returnValue = usersTravelFavoriteRepository.findByIdOrNull(user_id)
        //val temp = objectMapper.writeValueAsString(returnValue)
        //System.out.println(temp)
        return sendResponseHttpByJson("add $user_id 's travel favorite", returnValue!!)
    }

    private fun saveUsersTravelFavorite(user_id: String) {
        val usersTravelFavorites = UsersTravelFavorites(user_id)
        usersTravelFavoriteRepository.save(usersTravelFavorites)
    }
}