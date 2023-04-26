package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.exception.NotAcceptedException
import com.entrip.service.PlannersService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PlannersController(
    val plannersService: PlannersService
) : BaseController() {

    @PostMapping("/api/v1/planners/{user_id}")
    fun save(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val savedPlannerId: Long? = plannersService.save(user_id)
        val responseDto = plannersService.findByPlannerId(savedPlannerId!!)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Planner is saved well", returnDto)
    }

    @PutMapping("/api/v1/planners/{planner_id}")
    fun update(
        @PathVariable planner_id: Long,
        @RequestBody requestDto: PlannersUpdateRequestDto
    ): ResponseEntity<RestAPIMessages> {
        val updatedPlannerId: Long = plannersService.update(planner_id, requestDto)!!
        val responseDto = plannersService.findByPlannerId(updatedPlannerId)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Planner is updated well", returnDto)
    }

    @GetMapping("/api/v1/planners/{planner_id}")
    fun findById(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val responseDto = plannersService.findByPlannerId(planner_id)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Load planner with $planner_id", returnDto)
    }

    @DeleteMapping("/api/v1/planners/{planner_id}")
    fun delete(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Delete planner with id : $planner_id", plannersService.delete(planner_id))

    @DeleteMapping("api/v1/planners/{planner_id}/{user_id}/delete")
    fun deleteWithExit(@PathVariable planner_id: Long, @PathVariable user_id: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Delete planner ${planner_id}", plannersService.deleteWithExit(planner_id, user_id))

    @DeleteMapping("api/v1/planners/{planner_id}/{user_id}/exit")
    fun exitPlanner(@PathVariable planner_id: Long, @PathVariable user_id: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "User ${user_id} exit planner ${planner_id}",
            plannersService.exitPlanner(planner_id, user_id)
        )

    @PutMapping("api/v1/planners/{planner_id}/{user_id}")
    fun addFriendToPlanner(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(plannersService.addFriendToPlanner(planner_id, user_id), planner_id)

    @GetMapping("api/v1/planners/{planner_id}/getAllUser")
    fun findAllUsersWithPlannerId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Load all users with planner id : $planner_id",
            plannersService.findAllUsersWithPlannerId(planner_id)
        )

    @GetMapping("api/v1/planners/{planner_id}/all")
    fun findAllPlansWithPlannerId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Load all plans with specific planner id : $planner_id",
            plannersService.findAllPlansWithPlannerId(planner_id)
        )

    @GetMapping("api/v1/planners/{planner_id}/{date}/find")
    fun findByPlannerIdWithDate(
        @PathVariable planner_id: Long,
        @PathVariable date: String
    ): ResponseEntity<RestAPIMessages> = sendResponseHttpByJson(
        "Get all plans with specific date $date, planner_id $planner_id",
        plannersService.findByPlannerIdWithDate(planner_id, date)
    )

    @GetMapping("/api/v1/planners/{planner_id}/allNotices")
    fun findAllNoticesWithPlannerId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Get all notices with planner id : $planner_id",
            plannersService.findAllNoticesWithPlannerId(planner_id)
        )

    @GetMapping("/api/v1/planners/{planner_id}/allVotes")
    fun findAllVotesByPlannerId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Load votes with planner id : $planner_id",
            plannersService.findAllVotesWithPlannerID(planner_id)
        )

    @GetMapping("api/v1/planners/{planner_id}/exist")
    fun plannerIsExistWithId(@PathVariable planner_id: Long): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = plannersService.plannerIsExistWithId(planner_id)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Find if planner is exist with specific planner id : $planner_id", true)
    }

    @GetMapping("api/v1/planners/{planner_id}/{user_id}/exist")
    fun userIsExistInPlannerWithUserId(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = plannersService.userIsExistInPlannerWithUserId(planner_id, user_id)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if user : $user_id is exist at planner : $planner_id", true)
    }

    @GetMapping("api/v1/planners/{planner_id}/{nickname}/exist/nickname")
    fun userIsExistInPlannerWithNickname(
        @PathVariable planner_id: Long,
        @PathVariable nickname: String
    ): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = plannersService.userIsExistInPlannerWithNickname(planner_id, nickname)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if user : $nickname is exist at planner : $planner_id", true)
    }
}