package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import java.util.*
import javax.persistence.*

@Entity
class Users(
    @Id @Column(name = "USER_ID")
    val user_id: String? = null,

    @Column
    @ManyToMany (fetch = FetchType.EAGER)
    @JoinColumn(name = "PLANNERS_USERS")
    var planners : MutableSet<Planners> = TreeSet(),

    @Column
    @OneToMany (mappedBy = "users", fetch = FetchType.EAGER, orphanRemoval = true)
    //Check "orphanRemoval = true" is possible
    var comments : MutableSet<Comments> = TreeSet(),

    @Column
    val nickname: String,
    var gender : Int? = null,
    var photoUrl : String? = null,
    var token : String? = null

): BaseTimeEntity() {
    public fun addPlanners(planners : Planners) : Long? {
        this.planners.add(planners)
        if (!planners.users?.contains(this)!!)
            planners.addUsers(this)
        return planners.planner_id
    }
    public fun updateToken(token: String) : String {
        this.token = token
        return token
    }
}