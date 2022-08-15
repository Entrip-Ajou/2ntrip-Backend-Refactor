package com.entrip.domain.dto.Posts

import com.entrip.domain.dto.Photos.PhotosComparator
import com.entrip.domain.entity.Photos
import com.entrip.domain.entity.Posts
import java.util.Collections

class PostsRequestDto(
    val post_id: Long?,
    val title: String,
    val content: String,
    val author: String?,
    val likeNumber : Long,
    val commentsNumber : Long,
    var photoList: MutableList<Photos>? = ArrayList<Photos>()
) {
    constructor(entity: Posts) : this(
        post_id = entity.post_id,
        title = entity.title,
        content = entity.content,
        author = entity.author!!.user_id,
        likeNumber = entity.likeNumber,
        commentsNumber = entity.commentsNumber,
        photoList = entity.getPhotoListFromEntity(entity.photoSet)
    )

    public fun sortPhotoListWithPriority() = Collections.sort(photoList, PhotosComparator())

    public fun getPhotoListFromPostsRequestDto(): MutableList<String> {
        var photoList: MutableList<String> = ArrayList<String>()
        if (this.photoList != null) {
            for (photos in this.photoList!!) {
                val photoUrl: String = photos.photoUrl
                photoList.add(photoUrl)
            }
        }
        return photoList
    }
}