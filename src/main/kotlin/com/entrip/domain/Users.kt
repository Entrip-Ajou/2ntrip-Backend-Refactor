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
    var travelFavorite : String? = null,
    var photoUrl : String? = null,
    var token : String? = null

): BaseTimeEntity() {
    fun addPlanners(planners : Planners) : Long {
        this.planners.add(planners)
        //add user to planner code is required
        return 1 //Change return value to Planners_id
    }
    fun updateToken(token: String) : String {
        this.token = token
        return token
    }
}