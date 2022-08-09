package com.entrip.domain.entity

import javax.persistence.*

@Entity
class Comments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    val comment_id: Long? = null,

    @Column
    var author: String,
    var content: String,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "PLAN_ID")
    var plans: Plans? = null,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "USER_ID")
    var users: Users? = null
) {
    fun update(author: String, content: String): Unit {
        this.author = author
        this.content = content
    }

    //public Long setPlans
}