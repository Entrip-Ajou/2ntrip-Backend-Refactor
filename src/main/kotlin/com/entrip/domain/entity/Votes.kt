package com.entrip.domain.entity

import java.sql.Timestamp
import java.util.TreeSet
import javax.persistence.*

@Entity
class Votes(
    @Id
    @GeneratedValue
    @Column(name = "VOTE_ID")
    val vote_id : Long? = null,

    @Column
    var title : String,
    var multipleVote : Boolean,
    var anonymousVote : Boolean,
    var voting : Boolean,
    var deadLine : Timestamp,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    var author : Users? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PLANNER_ID")
    var planners: Planners? = null,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "VOTES")
    var contents : MutableSet<VotesContents> = TreeSet()

) {
}