package com.entrip.domain

import apple.laf.JRSUIUtils.Tree
import sun.security.ec.point.ProjectivePoint.Mutable
import java.util.TreeSet
import javax.persistence.*

@Entity
class Users(
    @Id @Column(name = "USER_ID")
    val user_id: String,

    @Column
    @ManyToMany (fetch = FetchType.EAGER)
    @JoinColumn(name = "PLANNERS_USERS")
    var planners : MutableSet<Planners> = TreeSet(),

    @Column
    @OneToMany (mappedBy = "users", fetch = FetchType.EAGER)
    var comments : MutableSet<Comments> = TreeSet(),

    @Column
    val nickname: String,
    var gender : Int? = null,
    var photoUrl : String? = null,
    var token : String? = null

): BaseTimeEntity() {
    fun addPlanners(planners : Planners) : Long {
        this.planners.add(planners)
        if (!planners.users.contains(this))
            planners.addUsers(this)
        return planners.planner_id
    }
    fun updateToken(token: String) : String {
        this.token = token
        return token
    }
}