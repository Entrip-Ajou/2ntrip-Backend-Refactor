package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import java.util.TreeSet
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class VotesContents (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VOTES_CONTENT_ID")
    val votesContent_id : Long? = null,

    @Column
    var contents : String,
    var selectedCount : Int = 0,

    @Column
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "VOTES_CONTENTS_USERS")
    var usersSet : MutableSet<Users> = TreeSet(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VOTE_ID")
    var votes : Votes? = null
) : BaseTimeEntity(), Comparable<VotesContents> {

    constructor(content: String) : this(
        contents = content
    )

    override fun compareTo(other: VotesContents): Int {
        if (this.votesContent_id!! > other.votesContent_id!!) return 1
        return -1
    }

    fun vote() {
        selectedCount++
    }
}