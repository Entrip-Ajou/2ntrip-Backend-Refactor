package com.entrip.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

// JWT를 생성하고 검증하는 컴포넌트
@Component
class JwtTokenProvider (private val userDetailsService : UserDetailsService) {
    private var logger : Logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private var secretKey = "2ntrip.com"

    // 토큰 유효 시간 : 30분
    private val tokenValidTime = 30*60*1000L

    // 객체 초기화. secretKey를 Base64로 인코딩
    @PostConstruct
    protected fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    // JWT 토큰 생성
     public fun createToken (userPk : String) : String {
        // JWT Payload에 저장되는 정보 단위
        val claims : Claims = Jwts.claims().setSubject(userPk)
        // Payload에 저장되는 key/value 쌍
        claims["userPK"] = userPk
        val now = Date()
        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenValidTime))
                // 암호화 알고리즘과 signature에 들어갈 secret 값 세팅
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
     }

    // JWT 토큰에서 인증 정보 조회
    fun getAuthentication (token : String): UsernamePasswordAuthenticationToken {
        val userDetails = userDetailsService.loadUserByUsername(getUserPk(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    // 토큰에서 회원 정보 추출
    fun getUserPk(token: String) : String {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
    }

    // Request의 Header에서 token 값을 가져옴
    fun resolveToken(request : HttpServletRequest) : String? =
        request.getHeader("Authorization")

    //토큰의 유효성 + 만료일자 확인
    fun validateToken(jwtToken : String): Boolean {
        return try {
            val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken)
            !claims.body.expiration.before(Date())
        } catch (e : Exception) {
            false
        }
    }
}