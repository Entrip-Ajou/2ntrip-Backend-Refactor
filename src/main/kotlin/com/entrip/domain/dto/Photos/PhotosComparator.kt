package com.entrip.domain.dto.Photos

import com.entrip.domain.entity.Photos

class PhotosComparator : Comparator<Photos> {
    public override fun compare(o1: Photos?, o2: Photos?): Int {
        if (o1!!.priority > o2!!.priority) return 1
        if (o1!!.priority < o2!!.priority) return -1
        return 0
    }
}