package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.exception.NotAcceptedException
import com.entrip.service.PlannersService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class PlannersController(
    final val plannersService: PlannersService
) {
    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

//    @PostMapping("/api/v1/planners")
//    public fun save (@RequestBody requestDto : PlannersSaveRequestDto) : ResponseEntity<Messages> {
//        val savedPlannerId : Long? = plannersService.save(requestDto)
//        val responseDto = plannersService.findByPlannerId(savedPlannerId!!)
//        val returnDto = PlannersReturnDto(responseDto)
//        return sendResponseHttpByJson("Planner is saved well", returnDto)
//    }

    @PostMapping("/api/v1/planners/{user_id}")
    public fun save(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val requestDto: PlannersSaveRequestDto = PlannersSaveRequestDto(user_id)
        val savedPlannerId: Long? = plannersService.save(requestDto)
        val responseDto = plannersService.findByPlannerId(savedPlannerId!!)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Planner is saved well", returnDto)
    }

    @PutMapping("/api/v1/planners/{planner_id}")
    public fun update(
        @PathVariable planner_id: Long,
        @RequestBody requestDto: PlannersUpdateRequestDto
    ): ResponseEntity<RestAPIMessages> {
        val updatedPlannerId: Long = plannersService.update(planner_id, requestDto)!!
        val responseDto = plannersService.findByPlannerId(updatedPlannerId)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Planner is updated well", returnDto)
    }

    @GetMapping("/api/v1/planners/{planner_id}")
    public fun findById(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val responseDto = plannersService.findByPlannerId(planner_id)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Load planner with $planner_id", returnDto)
    }

    @GetMapping("api/v1/planners/{planner_id}/all")
    public fun findAllPlansWithPlannerId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val plansList: MutableList<PlansReturnDto> = plannersService.findAllPlansWithPlannerId(planner_id)
        return sendResponseHttpByJson("Load all plans with specific planner id : $planner_id", plansList)
    }

    @GetMapping("api/v1/planners/{planner_id}/exist")
    public fun plannerIsExistWithId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = plannersService.plannerIsExistWithId(planner_id)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Find if planner is exist with specific planner id : $planner_id", isExist)
    }

    @DeleteMapping("/api/v1/planners/{planner_id}")
    public fun delete(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val deletedPlannerId: Long = plannersService.delete(planner_id)
        return sendResponseHttpByJson("Delete planner with id : $planner_id", deletedPlannerId)
    }

    @PutMapping("api/v1/planners/{planner_id}/{user_id}")
    public fun addFriendToPlanner(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> {
        return sendResponseHttpByJson(plannersService.addFriendToPlanner(planner_id, user_id), planner_id)
    }

    @GetMapping("api/v1/planners/{planner_id}/getAllUser")
    public fun findAllUsersWithPlannerId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val usersList: MutableList<UsersReturnDto> = plannersService.findAllUsersWithPlannerId(planner_id)
        return sendResponseHttpByJson("Load all users with planner id : $planner_id", usersList)
    }

    @GetMapping("api/v1/planners/{planner_id}/{user_id}/exist")
    public fun userIsExistInPlannerWithUserId(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = plannersService.userIsExistInPlannerWithUserId(planner_id, user_id)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if user : $user_id is exist at planner : $planner_id", isExist)
    }

    @GetMapping("api/v1/planners/{planner_id}/{nickname}/exist/nickname")
    public fun userIsExistInPlannerWithNickname(
        @PathVariable planner_id: Long,
        @PathVariable nickname: String
    ): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = plannersService.userIsExistInPlannerWithNickname(planner_id, nickname)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if user : $nickname is exist at planner : $planner_id", isExist)
    }

    @DeleteMapping("api/v1/planners/{planner_id}/{user_id}/delete")
    public fun deleteWithExit(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> {
        val long = plannersService.deleteWithExit(planner_id, user_id)
        return sendResponseHttpByJson("Delete planner ${planner_id}", planner_id)
    }

    @DeleteMapping("api/v1/planners/{planner_id}/{user_id}/exit")
    public fun exitPlanner(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> {
        val response: Boolean = plannersService.exitPlanner(planner_id, user_id)
        return sendResponseHttpByJson("User ${user_id} exit planner ${planner_id}", response)
    }

    @GetMapping("api/v1/planners/{planner_id}/{date}/find")
    public fun findByPlannerIdWithDate(
        @PathVariable planner_id: Long,
        @PathVariable date: String
    ): ResponseEntity<RestAPIMessages> {
        val response: MutableList<PlansReturnDto> = plannersService.findByPlannerIdWithDate(planner_id, date)
        return sendResponseHttpByJson("Get all plans with specific date $date, planner_id $planner_id", response)
    }
}