package com.entrip.domain

import javax.persistence.*

@Entity
class Users(
    @Id @Column
    val user_id: String,

    @Column
    @ManyToMany (fetch = FetchType.EAGER)
    @JoinColumn(name = "PLANNERS_USERS")
    var planners : HashSet<Planners> = HashSet(),

    @Column
    @OneToMany (mappedBy = "users", fetch = FetchType.EAGER)
    var comments : HashSet<Comments> = HashSet(),

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