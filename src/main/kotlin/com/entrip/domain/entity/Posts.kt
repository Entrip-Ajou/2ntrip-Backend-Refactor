package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import java.util.*
import javax.persistence.*

@Entity
class Posts(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    var post_id: Long? = null,

    @Column
    var title: String = "제목 없음",
    var content: String = "",
    var likeNumber: Long = 0L,
    var commentsNumber: Long = 0L,
    var postTag: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    var author: Users? = null,

    @OneToMany(mappedBy = "posts", fetch = FetchType.EAGER)
    var photoSet: MutableSet<Photos>? = TreeSet(),

    @OneToMany(mappedBy = "posts", fetch = FetchType.EAGER, orphanRemoval = true)
    var postsCommentsSet: MutableSet<PostsComments>? = TreeSet(),

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    var likeUsers: MutableSet<Users> = TreeSet()

    // var createdDate
    // var timeStamp
    // is included with BaseTimeEntity

) : BaseTimeEntity() {

    public fun update(title: String, content: String): Unit {
        this.title = title
        this.content = content
    }

    public fun getPhotoListFromEntity(photoSet: MutableSet<Photos>?): MutableList<Photos> {
        var photoList: MutableList<Photos> = ArrayList<Photos>()
        if (photoSet != null) {
            for (photo in photoSet) photoList.add(photo)
        }
        return photoList
    }

    public fun raiseLikeNumber(): Long =
        ++likeNumber

    public fun decreaseLikeNumber() : Long=
        --likeNumber

    public fun raiseCommentsNumber(): Long =
        ++commentsNumber

    public fun decreaseCommentsNumber() : Long =
        --commentsNumber

}