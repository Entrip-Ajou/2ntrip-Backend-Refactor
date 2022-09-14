package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import java.time.LocalDateTime
import java.util.TreeSet
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
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "USERS_PLANNERS")
    var users: MutableSet<Users> = TreeSet(),

    @Column
    @OneToMany(mappedBy = "planners", fetch = FetchType.EAGER)
    var plans: MutableSet<Plans>? = TreeSet(),

    @Column
    @OneToMany(mappedBy = "planners", fetch = FetchType.EAGER)
    var notices: MutableSet<Notices> = TreeSet()

) : BaseTimeEntity() {

    fun update(title: String, start_date: String, end_date: String): Unit {
        this.title = title
        this.start_date = start_date
        this.end_date = end_date
    }

    fun addUsers(users: Users): String? {
        this.users?.add(users)
        return users.user_id
    }

    fun setComment_timeStamp(): Unit {
        this.comment_timeStamp = LocalDateTime.now()
        return Unit
    }
}