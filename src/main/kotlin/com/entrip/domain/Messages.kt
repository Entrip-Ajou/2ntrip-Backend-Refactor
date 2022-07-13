package com.entrip.domain

import lombok.Data
import java.util.Objects

@Data
class Messages (
    val httpStatus : Int,
    val message : String,
    val data : Any
)