package com.entrip.domain

import java.util.StringJoiner
import java.util.TreeSet
import javax.persistence.*

@Entity
class Plans (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "PLAN_ID")
        val plan_id : Long,

        @Column
        var date : String,
        var todo : String,
        var time : String,
        var location : String,
        var rgb : Long,

        @ManyToOne (fetch = FetchType.EAGER)
        @JoinColumn (name = "PLANNER_ID")
        var planners: Planners,

        @OneToMany (mappedBy = "plans", fetch = FetchType.EAGER)
        var comments : MutableSet<Comments> = TreeSet()
        ) {
        fun update (date: String, todo : String, time : String, location : String, rgb : Long) : Unit {
                this.date = date
                this.todo = todo
                this.time = time
                this.location = location
                this.rgb = rgb
        }

        fun setPlanners (planners: Planners) : Long {
                this.planners = planners
                return this.planners.planner_id
        }

        fun isExistComments() : Boolean {
                return comments.isEmpty()
        }
}