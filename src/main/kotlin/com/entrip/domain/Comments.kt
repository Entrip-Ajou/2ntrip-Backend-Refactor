package com.entrip.domain

import javax.persistence.*

@Entity
class Comments (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val comment_id : Long,

    @Column
    var author : String,
    var content : String,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "PLAN_PLANS_ID")
    var plans : Plans,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    var users : Users,
        ){
}