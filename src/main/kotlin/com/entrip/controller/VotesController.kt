package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.VotesContents.VotesContentsCountRequestDto
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.Votes.VotesUpdateRequestDto
import com.entrip.domain.dto.VotesContents.PreviousVotesContentsRequestDto
import com.entrip.service.VotesContentsService
import com.entrip.service.VotesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class VotesController(
    val votesService : VotesService,

    @Autowired
    val votesContentsService: VotesContentsService
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

    @DeleteMapping("/api/v1/votes/{vote_id}")
    fun delete(@PathVariable vote_id : Long) : ResponseEntity<RestAPIMessages> {
        val voteId = votesService.delete(vote_id)
        return sendResponseHttpByJson("Delete votes with id : $voteId", vote_id)
    }

    @PutMapping("/api/v1/votes")
    fun update(@RequestBody requestDto: VotesUpdateRequestDto) : ResponseEntity<RestAPIMessages> {
        val voteId : Long? = votesService.update(requestDto)
        val returnDto = votesService.findById(voteId!!)
        return sendResponseHttpByJson("Votes is updated well", returnDto)
    }

    @GetMapping("/api/v1/votes/{vote_id}")
    fun findById(@PathVariable vote_id : Long) : ResponseEntity<RestAPIMessages> {
        val returnDto = votesService.getVotesInfoReturnDto(vote_id)
        return sendResponseHttpByJson("Load votes with id : $vote_id", returnDto)
    }

    @PostMapping("/api/v1/votes/{vote_id}")
    fun terminateVote(@PathVariable vote_id: Long) : ResponseEntity<RestAPIMessages> {
        votesService.terminateVote(vote_id)
        return sendResponseHttpByJson("Terminate vote with id : $vote_id", vote_id)
    }

    @PostMapping("/api/v1/votes/doVote")
    fun vote(@RequestBody requestDto: VotesContentsCountRequestDto) : ResponseEntity<RestAPIMessages> {
        val voteId = votesContentsService.vote(requestDto)
        val returnDto = votesService.getVotesInfoReturnDto(voteId!!)
        return sendResponseHttpByJson("successfully voted at $voteId", returnDto)
    }

    @PostMapping("/api/v1/votes/undoVote")
    fun undoVote(@RequestBody requestDto: VotesContentsCountRequestDto) : ResponseEntity<RestAPIMessages> {
        val voteId = votesContentsService.undoVote(requestDto)
        val returnDto = votesService.getVotesInfoReturnDto(voteId!!)
        return sendResponseHttpByJson("successfully undoVoted at $voteId", returnDto)
    }

    @PostMapping("api/v1/votes/getPreviousVotes")
    fun getPreviousVotes(@RequestBody requestDto: PreviousVotesContentsRequestDto) : ResponseEntity<RestAPIMessages> {
        val user_id = requestDto.user_id
        val returnDto = votesContentsService.getPreviousVoteContents(requestDto)
        return sendResponseHttpByJson("Load previous votesContents with $user_id", returnDto)
    }
}