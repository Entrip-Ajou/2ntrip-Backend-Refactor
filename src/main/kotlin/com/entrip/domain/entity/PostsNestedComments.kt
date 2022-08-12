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
    var author: Users? = null,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "POST_COMMENT_ID")
    var postsComments: PostsComments? = null
) {

    public fun setAuthorWithJoin(users: Users) {
        this.author = users
        author!!.postsNestedComments.add(this)
    }

    public fun setPostsCommentsWithJoin(postsComments: PostsComments?) {
        this.postsComments = postsComments
        postsComments!!.postsNestedComments.add(this)
    }
}