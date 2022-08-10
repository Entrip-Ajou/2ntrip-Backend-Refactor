package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import javax.persistence.*

@Entity
class PostsComments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_COMMENT_ID")
    var postComment_id: Long? = null,

    @Column
    var content: String,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "USER_ID")
    var author: Users? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "POST_ID")
    var posts: Posts? = null
) : BaseTimeEntity() {

    public fun setAuthorWithJoin(users: Users) {
        this.author = users
        users.postsComments.add(this)
    }

    public fun setPostsWithJoin(posts: Posts) {
        this.posts = posts
        posts.postsCommentsSet?.add(this)
    }
}