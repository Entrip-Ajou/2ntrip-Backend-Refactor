package com.entrip.domain

import lombok.Data

@Data
class Messages(
    val httpStatus: Int,
    val message: String,
    val data: Any
)