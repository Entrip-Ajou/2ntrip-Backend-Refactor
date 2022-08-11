package com.entrip.domain.entity

import javax.persistence.*

@Entity
class PostsNestedComments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_NESTED_COMMENT_ID")
    var postNestedComment_id: Long? = null,

    @Column
    var content: String,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "USER_ID")
    var author: Users? = null
) {

}