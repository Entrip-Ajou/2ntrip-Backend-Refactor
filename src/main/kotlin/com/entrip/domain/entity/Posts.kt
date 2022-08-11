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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    var author: Users? = null,

    @OneToMany(mappedBy = "posts", fetch = FetchType.EAGER)
    var photoSet: MutableSet<Photos>? = TreeSet(),

    @OneToMany(mappedBy = "posts", fetch = FetchType.EAGER, orphanRemoval = true)
    var postsCommentsSet: MutableSet<PostsComments>? = TreeSet(),

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

}