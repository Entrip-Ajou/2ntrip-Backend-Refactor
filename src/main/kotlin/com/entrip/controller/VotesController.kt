package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.Votes.VotesUpdateRequestDto
import com.entrip.domain.dto.VotesContents.PreviousVotesContentsRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsCountRequestDto
import com.entrip.service.VotesContentsService
import com.entrip.service.VotesService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class VotesController(
    val votesService: VotesService,
    val votesContentsService: VotesContentsService
) : BaseController() {

    // 투표 작성 리턴 VotesReturnDto
    @PostMapping("/api/v1/votes")
    fun save(@RequestBody requestDto: VotesSaveRequestDto): ResponseEntity<RestAPIMessages> {
        val voteId: Long = votesService.save(requestDto)!!
        val returnDto = votesService.findById(voteId)
        return sendResponseHttpByJson("Votes is saved well", returnDto)
    }

    @DeleteMapping("/api/v1/votes/{vote_id}")
    fun delete(@PathVariable vote_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Delete votes with id : $vote_id", votesService.delete(vote_id))


    @PutMapping("/api/v1/votes")
    fun update(@RequestBody requestDto: VotesUpdateRequestDto): ResponseEntity<RestAPIMessages> {
        val voteId: Long? = votesService.update(requestDto)
        val returnDto = votesService.findById(voteId!!)
        return sendResponseHttpByJson("Votes is updated well", returnDto)
    }

    @GetMapping("/api/v1/votes/{vote_id}")
    fun findById(@PathVariable vote_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Load votes with id : $vote_id", votesService.getVotesInfoReturnDto(vote_id))


    @PostMapping("/api/v1/votes/{vote_id}")
    fun terminateVote(@PathVariable vote_id: Long): ResponseEntity<RestAPIMessages> {
        votesService.terminateVote(vote_id)
        return sendResponseHttpByJson("Terminate vote with id : $vote_id", vote_id)
    }

    @PostMapping("/api/v1/votes/doVote")
    fun vote(@RequestBody requestDto: VotesContentsCountRequestDto): ResponseEntity<RestAPIMessages> {
        val voteId = votesContentsService.vote(requestDto)
        return sendResponseHttpByJson("successfully voted at $voteId", votesService.getVotesInfoReturnDto(voteId!!))
    }

    @PostMapping("/api/v1/votes/undoVote")
    fun undoVote(@RequestBody requestDto: VotesContentsCountRequestDto): ResponseEntity<RestAPIMessages> {
        val voteId = votesContentsService.undoVote(requestDto)
        return sendResponseHttpByJson("successfully undoVoted at $voteId", votesService.getVotesInfoReturnDto(voteId!!))
    }

    @PostMapping("api/v1/votes/getPreviousVotes")
    fun getPreviousVotes(@RequestBody requestDto: PreviousVotesContentsRequestDto): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Load previous votesContents with ${requestDto.user_id}",
            votesContentsService.getPreviousVoteContents(requestDto)
        )

}