package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.Votes.VotesUpdateRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.service.VotesService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class VotesController(
    val votesService : VotesService
) {
    private fun sendResponseHttpByJson(message: String, data: Any) : ResponseEntity<RestAPIMessages> {
        val restAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

    // 투표 작성 리턴 VotesReturnDto
    @PostMapping("/api/v1/votes")
    fun save(@RequestBody requestDto : VotesSaveRequestDto) : ResponseEntity<RestAPIMessages> {
        val voteId : Long = votesService.save(requestDto)!!
        val returnDto = votesService.findById(voteId)
        return sendResponseHttpByJson("Votes is saved well", returnDto)
    }

    // 투표 조회 리턴 VotesListReturnDto (전체 투표 리스트 조회)
    @GetMapping("/api/v1/votes/{planner_id}")
    fun findAllVotesByPlannerId(@PathVariable planner_id :Long) : ResponseEntity<RestAPIMessages> {
        val votesList : MutableList<VotesReturnDto>  = votesService.findAllVotesWithPlannerID(planner_id)

        return sendResponseHttpByJson("Load votes with planner id : $planner_id", votesList)
    }

    @DeleteMapping("/api/v1/votes/{vote_id}")
    fun delete(@PathVariable vote_id : Long) : ResponseEntity<RestAPIMessages> {
        val voteId = votesService.delete(vote_id)
        return sendResponseHttpByJson("Delete votes with id : $voteId", vote_id)
    }

    @PutMapping("/api/v1/votes/{vote_id}")
    fun update(@RequestBody requestDto: VotesUpdateRequestDto) : ResponseEntity<RestAPIMessages> {
        val voteId : Long? = votesService.update(requestDto)
        val returnDto = votesService.findById(voteId!!)
        return sendResponseHttpByJson("Votes is updated well", returnDto)
    }
}