package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import org.springframework.cache.annotation.CachePut
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*
import javax.persistence.*

@Entity
@EnableJpaAuditing
class Users(
    @Id @Column(name = "USER_ID")
    val user_id: String? = null,

    @Column
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "PLANNERS_USERS")
    var planners: MutableSet<Planners> = TreeSet(),

    @Column
    @OneToMany(mappedBy = "users", fetch = FetchType.EAGER, orphanRemoval = true)
    //Check "orphanRemoval = true" is possible
    var comments: MutableSet<Comments> = TreeSet(),

    @Column
    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER, orphanRemoval = true)
    var posts: MutableSet<Posts> = TreeSet(),


    @Column
    val nickname: String,
    var gender: Int? = null,
    var photoUrl: String? = null,
    var token: String? = null

) : BaseTimeEntity(), Comparable<Users> {
    public fun addPlanners(planners: Planners): Long? {
        this.planners.add(planners)
        return planners.planner_id
    }

    public fun updateToken(token: String): String {
        this.token = token
        return token
    }

    public override fun compareTo(other: Users): Int {
        if (this.user_id!! >= other.user_id!!) return 1;
        return -1;
    }
}