package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.service.PlannersService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset
import javax.xml.ws.Response

@RestController
class PlannersController (
    final val plannersService : PlannersService
        ){
    private fun sendResponseHttpByJson (message : String?, data : Any?) : ResponseEntity<Messages> {
        val messages : Messages = Messages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers : HttpHeaders = HttpHeaders()
        headers.contentType = MediaType ("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<Messages>(messages, headers, HttpStatus.OK)
    }

    @PostMapping("/api/v1/planners")
    public fun save (@RequestBody requestDto : PlannersSaveRequestDto) : ResponseEntity<Messages> {
        val savedPlannerId : Long = plannersService.save(requestDto)!!
        val responseDto = plannersService.findByPlannerId(savedPlannerId)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Planner is saved well", returnDto)
    }

    @PutMapping("/api/v1/planners/{planner_id}")
    public fun update (@PathVariable planner_id : Long, @RequestBody requestDto: PlannersUpdateRequestDto) : ResponseEntity<Messages> {
        val updatedPlannerId : Long = plannersService.update(planner_id, requestDto)!!
        val responseDto = plannersService.findByPlannerId(updatedPlannerId)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Planner is updated well", returnDto)
    }


}