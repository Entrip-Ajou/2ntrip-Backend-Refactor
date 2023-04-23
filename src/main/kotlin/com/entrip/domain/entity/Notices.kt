package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import javax.persistence.*

@Entity
class Notices (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTICE_ID")
    val notice_id : Long? = null,

    @Column
    var title: String,
    var content: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    var author: Users? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PLANNER_ID")
    var planners: Planners? = null

) : BaseTimeEntity(), Comparable<Notices> {
    fun update(title: String, content: String): Unit {
        this.title = title
        this.content = content
    }

    override fun compareTo(other: Notices): Int {
        return 1
    }
}