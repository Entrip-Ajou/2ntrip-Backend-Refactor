package com.entrip.auth.jwt

import com.entrip.exception.ExpiredAccessTokenException
import com.entrip.exception.ExpiredRefreshTokenException
import com.entrip.exception.ReIssueBeforeAccessTokenExpiredException
import com.entrip.service.RedisService
import io.jsonwebtoken.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

// JWT를 생성하고 검증하는 컴포넌트
@Component
class JwtTokenProvider(
    private val userDetailsService: UserDetailsService,
    private final val redisService: RedisService,
    @Value("#{security['spring.encryption.key']}")
    private var secretKey: String
) {
    private var logger: Logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)


    // 객체 초기화. secretKey를 Base64로 인코딩
    @PostConstruct
    protected fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    // JWT 토큰 생성
    private fun createToken(userPk: String, tokenValidtime: Long): String {
        val claims: Claims = Jwts.claims().setSubject(userPk)
        // Payload에 저장되는 key/value 쌍
        claims["userPK"] = userPk
        val now = Date()
        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenValidtime))
            // 암호화 알고리즘과 signature에 들어갈 secret 값 세팅
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    public fun createAccessToken(userPk: String): String {
        //token Valid Time : 1 min
        val tokenValidTime = 1 * 60 * 1000L
        val accessToken = createToken(userPk, tokenValidTime)
        redisService.setValues(userPk + "A", accessToken, Duration.ofMillis(tokenValidTime))
        return accessToken
    }

    public fun createRefreshToken(userPk: String): String {
        //token Valid Time : 1 day
        val tokenValidTime = 3600 * 60 * 1000L
        val refreshToken = createToken(userPk, tokenValidTime)
        redisService.setValues(userPk + "R", refreshToken, Duration.ofMillis(tokenValidTime))
        return refreshToken
    }

    // JWT 토큰에서 인증 정보 조회
    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val userDetails = userDetailsService.loadUserByUsername(getUserPk(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    // 토큰에서 회원 정보 추출
    fun getUserPk(token: String): String {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
    }

    // Request의 Header에서 AccessToken 값을 가져옴
    fun resolveAccessToken(request: HttpServletRequest): String? =
        request.getHeader("AccessToken")

    //토큰의 유효성 + 만료일자 확인
    fun validateToken(jwtToken: String?): Boolean {
        return try {
            val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken)
            !claims.body.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            throw ExpiredAccessTokenException("Token was expired!")
        } catch (e: SignatureException) {
            throw SignatureException("Token is not valid!")
        }
    }

    fun validateAccessToken(accessToken: String): Boolean {
        val userPk = getUserPk(accessToken) + "A"
        val redisRT: String = redisService.getValues(userPk)
            ?: throw ExpiredAccessTokenException("Access token was expired. Please reissue.")
        if (redisRT != redisService.getValues(userPk)) {
            expireTokenManually(accessToken, "A")
            throw SignatureException("Token is not valid!")
        }
        return validateToken(accessToken)
    }

    fun validateRefreshToken(refreshToken: String): Boolean {
        val userPk = getUserPk(refreshToken!!) + "R"
        val redisRT: String? = redisService.getValues(userPk)
            ?: throw ExpiredRefreshTokenException("Refresh token was expired. Please re-login.")
        if (redisRT != redisService.getValues(userPk)) {
            expireTokenManually(refreshToken, "R")
            throw SignatureException("Refresh Token is not valid!")
        }
        return validateToken(refreshToken)
    }

    fun reIssue(refreshToken: String): String {
        val user_id = getUserPk(refreshToken)
        if (!checkAccessTokenIsExpiredInRedis(user_id)) {
            expireTokenManually(refreshToken, "R")
            throw ReIssueBeforeAccessTokenExpiredException("ReIssue before Access Token Expired !!!")
        }
        validateRefreshToken(refreshToken)
        return createAccessToken(user_id)
    }

    fun checkAccessTokenIsExpiredInRedis(userPk: String): Boolean =
        redisService.getValues(userPk + "A") == null

    fun expireTokenManually(token: String, flag: String) =
        removeTokenFromRedis(getUserPk(token) + flag)

    fun removeTokenFromRedis(userPk: String) =
        redisService.deleteValues(userPk)
}