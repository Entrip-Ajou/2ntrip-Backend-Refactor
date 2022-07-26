package com.entrip.domain.entity

import java.util.TreeSet
import javax.persistence.*

@Entity
class Plans (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "PLAN_ID")
        val plan_id : Long? = null,

        @Column
        var date : String,
        var todo : String,
        var time : String,
        var location : String? = null,
        var rgb : Long,

        @ManyToOne (fetch = FetchType.EAGER)
        @JoinColumn (name = "PLANNER_ID")
        var planners: Planners? = null,

        @OneToMany (mappedBy = "plans", fetch = FetchType.EAGER)
        var comments : MutableSet<Comments> = TreeSet()
        ) {
        public fun update (date: String, todo : String, time : String, location : String?, rgb : Long) : Unit {
                this.date = date
                this.todo = todo
                this.time = time
                this.location = location
                this.rgb = rgb
        }

        public fun setPlanners (planners: Planners) : Long? {
                this.planners = planners
                return this.planners!!.planner_id
        }

        public fun isExistComments() : Boolean {
                return comments.isNotEmpty()
        }
}