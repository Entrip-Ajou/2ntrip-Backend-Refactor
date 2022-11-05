package com.entrip.domain.entity

import java.time.LocalDateTime
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
    var deadLine : LocalDateTime?,

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
    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateAnonymousVote(anonymousVote: Boolean) {
        this.anonymousVote = anonymousVote
    }

    fun updateMultipleVote(multipleVote: Boolean) {
        this.multipleVote = multipleVote
    }

    fun updateDeadLine(deadLine: LocalDateTime) {
        this.deadLine = deadLine
    }

    fun terminate() {
        this.voting = false
    }

}