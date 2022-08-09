package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Plans.PlansUpdateRequestDto
import com.entrip.service.PlansService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class PlansController(
    final val plansService: PlansService
) {

    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<Messages> {
        val messages: Messages = Messages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<Messages>(messages, headers, HttpStatus.OK)
    }

    @PostMapping("/api/v1/plans")
    public fun save(@RequestBody requestDto: PlansSaveRequestDto): ResponseEntity<com.entrip.domain.Messages> {
        val savedPlanId: Long = plansService.save(requestDto)!!
        val responseDto: PlansResponseDto = plansService.findById(savedPlanId!!)
        val returnDto: PlansReturnDto = PlansReturnDto(responseDto)
        return sendResponseHttpByJson("Plan is saved well", returnDto)
    }

    @PutMapping("/api/v1/plans/{plan_id}")
    public fun update(
        @PathVariable plan_id: Long,
        @RequestBody requestDto: PlansUpdateRequestDto
    ): ResponseEntity<Messages> {
        val updatedPlannerId = plansService.update(plan_id, requestDto)
        val responseDto = plansService.findById(plan_id)
        val returnDto = PlansReturnDto(responseDto)
        return sendResponseHttpByJson("Plan is updated well", returnDto)
    }

    @GetMapping("/api/v1/plans/{plan_id}")
    public fun findById(@PathVariable plan_id: Long): ResponseEntity<Messages> {
        val responseDto = plansService.findById(plan_id)
        val returnDto = PlansReturnDto(responseDto)
        return sendResponseHttpByJson("Load plan with id : $plan_id", returnDto)
    }

    @DeleteMapping("/api/v1/plans/{plan_id}")
    public fun delete(@PathVariable plan_id: Long): ResponseEntity<Messages> {
        val deletedPlanId = plansService.delete(plan_id)
        return sendResponseHttpByJson("Delete plan with id : $plan_id", deletedPlanId)
    }
}