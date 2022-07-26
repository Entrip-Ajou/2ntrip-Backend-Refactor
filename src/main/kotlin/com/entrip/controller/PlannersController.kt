package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.service.PlannersService
import com.sun.org.apache.xpath.internal.operations.Bool
import org.springframework.data.repository.config.RepositoryNameSpaceHandler
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
    private fun sendResponseHttpByJson (message : String, data : Any) : ResponseEntity<Messages> {
        val messages : Messages = Messages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers : HttpHeaders = HttpHeaders()
        headers.contentType = MediaType ("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<Messages>(messages, headers, HttpStatus.OK)
    }

//    @PostMapping("/api/v1/planners")
//    public fun save (@RequestBody requestDto : PlannersSaveRequestDto) : ResponseEntity<Messages> {
//        val savedPlannerId : Long? = plannersService.save(requestDto)
//        val responseDto = plannersService.findByPlannerId(savedPlannerId!!)
//        val returnDto = PlannersReturnDto(responseDto)
//        return sendResponseHttpByJson("Planner is saved well", returnDto)
//    }

    @PostMapping("/api/v1/planners/{user_id}")
    public fun save (@PathVariable user_id: String) : ResponseEntity<Messages> {
        val requestDto : PlannersSaveRequestDto = PlannersSaveRequestDto(user_id)
        val savedPlannerId : Long? = plannersService.save(requestDto)
        val responseDto = plannersService.findByPlannerId(savedPlannerId!!)
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

    @GetMapping("/api/v1/planners/{planner_id}")
    public fun findById (@PathVariable planner_id : Long) : ResponseEntity<Messages> {
        val responseDto = plannersService.findByPlannerId(planner_id)
        val returnDto = PlannersReturnDto(responseDto)
        return sendResponseHttpByJson("Load planner with $planner_id", returnDto)
    }

    @GetMapping("api/v1/planners/{planner_id}/all")
    public fun findAllPlansWithPlannerId (@PathVariable planner_id: Long) : ResponseEntity<Messages> {
        val plansList : MutableList<PlansReturnDto> = plannersService.findAllPlansWithPlannerId(planner_id)
        return sendResponseHttpByJson("Load all plans with specific planner id : $planner_id", plansList)
    }

    @GetMapping("api/v1/planners/{planner_id}/exist")
    public fun plannerIsExistWithId (@PathVariable planner_id: Long) : ResponseEntity<Messages> {
        val isExist : Boolean = plannersService.plannerIsExistWithId(planner_id)
        return sendResponseHttpByJson("Find if planner is exist with specific planner id : $planner_id", isExist)
    }

    @DeleteMapping("/api/v1/planners/{planner_id}")
    public fun delete (@PathVariable planner_id: Long) : ResponseEntity<Messages> {
        val deletedPlannerId : Long = plannersService.delete(planner_id)
        return sendResponseHttpByJson("Delete planner with id : $planner_id", deletedPlannerId)
    }

    @PutMapping("api/v1/planners/{planner_id}/{user_id}")
    public fun addFriendToPlanner (@PathVariable planner_id: Long, @PathVariable user_id : String) : ResponseEntity<Messages> {
        return sendResponseHttpByJson(plannersService.addFriendToPlanner(planner_id, user_id), planner_id)
    }

    @GetMapping("api/v1/planners/{planner_id}/getAllUser")
    public fun findAllUsersWithPlannerId (@PathVariable planner_id: Long) : ResponseEntity<Messages> {
        val usersList : MutableList<UsersReturnDto> = plannersService.findAllUsersWithPlannerId(planner_id)
        return sendResponseHttpByJson("Load all users with planner id : $planner_id", usersList)
    }

    @GetMapping("api/v1/planners/{planner_id}/{user_id}/exist")
    public fun userIsExistWithPlanner (@PathVariable planner_id : Long, @PathVariable user_id : String) : ResponseEntity<Messages> {
        val isExist : Boolean = plannersService.userIsExistWithPlanner(planner_id, user_id)
        return sendResponseHttpByJson("Check if user : $user_id is exist at planner : $planner_id", isExist)
    }
}