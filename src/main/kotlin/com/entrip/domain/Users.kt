package com.entrip.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Users(
    @Id @Column(name = "user_id")
    val user_id: String,

) {

}