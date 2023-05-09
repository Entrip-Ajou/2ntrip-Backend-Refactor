package com.entrip.repository

import com.entrip.domain.entity.AccessToken
import org.springframework.data.repository.CrudRepository

interface AccessTokenRepository : CrudRepository<AccessToken, String> {
}