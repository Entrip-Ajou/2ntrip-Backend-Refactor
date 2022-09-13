package com.entrip.service

import com.entrip.domain.dto.Notices.NoticesReturnDto
import com.entrip.domain.dto.Notices.NoticesSaveRequestDto
import com.entrip.domain.dto.Notices.NoticesUpdateRequestDto
import com.entrip.domain.entity.Notices
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.NoticesRepository
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class NoticesService (
    val noticesRepository: NoticesRepository,

    @Autowired
    private val plannersRepository: PlannersRepository,

    @Autowired
    private val usersRepository: UsersRepository,
) {

    @Transactional
    fun save(requestDto: NoticesSaveRequestDto) : Long? {
        val plannerId = requestDto.planner_id
        val planners: Planners = plannersRepository.findById(plannerId).orElseThrow {
            NotAcceptedException(Unit)
        }
        val author = findUsers(requestDto.author)
        val notices = requestDto.toEntity()
        notices.author = author
        notices.planners = planners
        planners.notices.add(notices)
        author.notices.add(notices)

        return noticesRepository.save(notices).notice_id
    }

    @Transactional
    fun update(notice_id: Long, requestDto: NoticesUpdateRequestDto) : Long? {
        val notices = findNotices(notice_id)
        notices.update(requestDto.title, requestDto.content)

        return notice_id
    }

    private fun findUsers(user_id: String?): Users {
        val users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    fun findById(notice_id: Long): NoticesReturnDto {
        val notices = findNotices(notice_id)
        return NoticesReturnDto(notices)
    }

    private fun findNotices(notice_id: Long) : Notices {
        val notices = noticesRepository.findById(notice_id).orElseThrow {
            IllegalArgumentException("Error raise at noticesRepository.findById$notice_id")
        }
        return notices
    }
}