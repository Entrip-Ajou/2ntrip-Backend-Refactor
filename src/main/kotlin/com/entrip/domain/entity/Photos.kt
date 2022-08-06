package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import javax.persistence.*

@Entity
class Photos (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "PHOTO_ID")
        val photo_id : Long? = null,

        @Column
        val photoUrl : String,
        val fileName : String,

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn (name = "POST_ID")
        var posts: Posts? = null

        // var createdDate
        // var timeStamp
        // is included with BaseTimeEntity
) : BaseTimeEntity() {
        // Have to make functions
}