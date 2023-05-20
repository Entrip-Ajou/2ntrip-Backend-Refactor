package com.entrip.domain.entity

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Plans(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAN_ID")
    val plan_id: Long? = null,

    @Column
    var date: String,
    var todo: String,
    var time: String,
    var location: String? = null,
    var rgb: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLANNER_ID")
    var planners: Planners? = null,

    @OneToMany(mappedBy = "plans", fetch = FetchType.LAZY, orphanRemoval = true)
    var comments: MutableSet<Comments> = TreeSet()
) : Comparable<Plans> {
    fun update(date: String, todo: String, time: String, location: String?, rgb: Long): Unit {
        this.date = date
        this.todo = todo
        this.time = time
        this.location = location
        this.rgb = rgb
    }

    fun setPlanners(planners: Planners): Long? {
        this.planners = planners
        return this.planners!!.planner_id
    }

    fun isExistComments(): Boolean {
        return comments.isNotEmpty()
    }

    override fun compareTo(other: Plans): Int {
        return 1
    }

    companion object {
        fun createPlans(planners: Planners, plans: Plans): Plans {
            plans.setPlanners(planners)
            planners.plans!!.add(plans)

            planners.setTimeStamp(LocalDateTime.now())

            return plans
        }
    }
}