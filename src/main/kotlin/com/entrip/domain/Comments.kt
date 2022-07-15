package com.entrip.domain

import javax.persistence.*

@Entity
class Comments (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    val comment_id : Long,

    @Column
    var author : String,
    var content : String,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "PLAN_ID")
    var plans : Plans,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "USER_ID")
    var users : Users
        ){
    fun update (author : String, content : String) : Unit {
        this.author = author
        this.content = content
    }

    //public Long setPlans
}