package com.entrip.domain.entity

import apple.laf.JRSUIUtils.Tree
import com.entrip.domain.BaseTimeEntity
import sun.security.ec.point.ProjectivePoint.Mutable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class Posts (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column (name = "POST_ID")
        var post_id : Long? = null,

        @Column
        var title : String = "제목 없음",
        var content : String = "",

        @ManyToOne (fetch = FetchType.EAGER)
        var author : Users,

        @OneToMany (mappedBy = "posts", fetch = FetchType.EAGER)
        var photoSet : MutableSet<Photos> = TreeSet()

        // var createdDate
        // var timeStamp
        // is included with BaseTimeEntity

) : BaseTimeEntity() {
        // Have to make functions
}