package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.*

@Entity
class Planners(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLANNER_ID")
    var planner_id: Long? = null,

    @Column
    var title: String = "제목 없음",
    var start_date: String,
    var end_date: String,
    var comment_timeStamp: LocalDateTime = LocalDateTime.now(),

    @Column
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_PLANNERS")
    var users: MutableSet<Users> = TreeSet(),

    @Column
    @OneToMany(mappedBy = "planners", fetch = FetchType.LAZY)
    var plans: MutableSet<Plans>? = TreeSet(),

    @Column
    @OneToMany(mappedBy = "planners", fetch = FetchType.LAZY)
    var notices: MutableSet<Notices> = TreeSet(),

    @Column
    @OneToMany(mappedBy = "planners", fetch = FetchType.LAZY)
    var votes: MutableSet<Votes> = TreeSet(),

    ) : BaseTimeEntity(), Comparable<Planners> {

    fun update(title: String, start_date: String, end_date: String): Unit {
        this.title = title
        this.start_date = start_date
        this.end_date = end_date
    }

    fun addUsers(users: Users): String {
        this.users.add(users)
        return users.user_id
    }

    fun setComment_timeStamp() {
        this.comment_timeStamp = LocalDateTime.now()
        return
    }

    override fun compareTo(other: Planners): Int {
        return 1
    }

    companion object {
        fun createPlanners(users: Users): Planners {
            val time: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

            val planner = Planners(
                title = "제목 없음",
                start_date = time,
                end_date = time
            )

            planner.setComment_timeStamp()
            planner.addUsers(users)
            users.addPlanners(planner)

            return planner
        }
    }
}