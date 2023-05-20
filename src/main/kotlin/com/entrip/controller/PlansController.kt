package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Plans.PlansUpdateRequestDto
import com.entrip.service.PlansService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PlansController(
    val plansService: PlansService
) : BaseController() {

    @PostMapping("/api/v1/plans")
    fun save(@RequestBody requestDto: PlansSaveRequestDto): ResponseEntity<RestAPIMessages> {
        val savedPlanId: Long = plansService.save(requestDto)!!
        val responseDto: PlansResponseDto = plansService.findById(savedPlanId)
        return sendResponseHttpByJson("Plan is saved well", responseDto)
    }

    @PutMapping("/api/v1/plans/{plan_id}")
    fun update(
        @PathVariable plan_id: Long,
        @RequestBody requestDto: PlansUpdateRequestDto
    ): ResponseEntity<RestAPIMessages> {
        plansService.update(plan_id, requestDto)
        val responseDto = plansService.findById(plan_id)
        return sendResponseHttpByJson("Plan is updated well", responseDto)
    }

    @GetMapping("/api/v1/plans/{plan_id}")
    fun findById(@PathVariable plan_id: Long): ResponseEntity<RestAPIMessages> {
        val responseDto = plansService.findById(plan_id)
        return sendResponseHttpByJson("Load plan with id : $plan_id", responseDto)
    }

    @DeleteMapping("/api/v1/plans/{plan_id}")
    fun delete(@PathVariable plan_id: Long): ResponseEntity<RestAPIMessages> {
        val deletedPlanId = plansService.delete(plan_id)
        return sendResponseHttpByJson("Delete plan with id : $plan_id", deletedPlanId)
    }
}