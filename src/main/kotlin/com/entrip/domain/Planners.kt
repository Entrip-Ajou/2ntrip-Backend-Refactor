package com.entrip.domain

import java.time.LocalDateTime
import java.util.TreeSet
import javax.persistence.*

@Entity
class Planners (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var planner_id : Long,

        @Column
        var title : String,
        var start_date : String,
        var end_date : String,
        var comment_timeStamp : LocalDateTime,

        @Column
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinColumn(name = "USERS_PLANNERS")
        var users : MutableSet<Users> = TreeSet(),

        @Column
        @OneToMany (mappedBy = "planners", fetch = FetchType.EAGER)
        var plans : MutableSet<Plans> = TreeSet()
        ){
        fun Planners (title : String, start_date: String, end_date: String) {
                this.title = title
                this.start_date = start_date
                this.end_date = end_date
        }

        fun update (title : String, start_date: String, end_date: String) {
                this.title = title
                this.start_date = start_date
                this.end_date = end_date
        }

        fun addUsers (users : Users) : String {
                this.users.add(users)
                if (!users.planners.contains(this))
                        users.addPlanners(this)
                return users.user_id
        }

        fun setComment_timeStamp () : Unit {
                this.comment_timeStamp = LocalDateTime.now()
                return Unit
        }
}