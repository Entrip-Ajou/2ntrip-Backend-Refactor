package com.entrip.domain.entity

import com.entrip.domain.BaseTimeEntity
import com.sun.org.apache.xpath.internal.operations.Bool
import javax.persistence.*

@Entity
class Photos(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PHOTO_ID")
    val photo_id: Long? = null,

    @Column
    val photoUrl: String,
    val fileName: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "POST_ID")
    var posts: Posts? = null,

    @Column
    var priority: Long = 1

    // var createdDate
    // var timeStamp
    // is included with BaseTimeEntity
) : BaseTimeEntity(), Comparable<Photos> {
    public override fun compareTo(other: Photos): Int {
        if (this.priority > other.priority) return 1
        if (this.priority < other.priority) return -1
        return 0
    }

}

fun Photos.isCommunity(): Boolean = !(this.priority == -1L)