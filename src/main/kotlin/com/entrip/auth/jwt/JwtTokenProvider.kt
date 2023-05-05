package com.entrip.auth.jwt

import com.entrip.exception.authException.ExpiredAccessTokenException
import com.entrip.exception.authException.ExpiredRefreshTokenException
import com.entrip.exception.authException.ReIssueBeforeAccessTokenExpiredException
import com.entrip.service.RedisService
import io.jsonwebtoken.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

// JWT를 생성하고 검증하는 컴포넌트
@Component
class JwtTokenProvider(
    private val userDetailsService: UserDetailsService,
    private val redisService: RedisService,
    @Value("#{security['spring.encryption.key']}")
    private var secretKey: String
) {
    private var logger: Logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    var accessTokenValidTime: Long = 10 * 60L
    var refreshTokenValidTime: Long = 3600 * 60L


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
            .setExpiration(Date(now.time + tokenValidtime * 1000L))
            // 암호화 알고리즘과 signature에 들어갈 secret 값 세팅
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun createAccessToken(userPk: String): String {
        //token Valid Time : 1 min
        val accessToken = createToken(userPk, accessTokenValidTime)
        redisService.saveAccessToken(userPk, accessToken, accessTokenValidTime)
        return accessToken
    }

    fun createRefreshToken(userPk: String): String {
        //token Valid Time : 1 day
        val refreshToken = createToken(userPk, refreshTokenValidTime)
        redisService.saveRefreshToken(userPk, refreshToken, refreshTokenValidTime)
        return refreshToken
    }

    // JWT 토큰에서 인증 정보 조회
    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val userDetails = userDetailsService.loadUserByUsername(getUserPk(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    // 토큰에서 회원 정보 추출
    // userPk 의 Signature가 맞지 않다면 SignatureException Raise
    fun getUserPk(token: String): String {
        return try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
        } catch (e: SignatureException) {
            logger.info("Signature Exception at getUserPk")
            throw SignatureException("Token is not valid!")
        }
    }

    // Request의 Header에서 AccessToken 값을 가져옴
    fun resolveAccessToken(request: HttpServletRequest): String? =
        request.getHeader("AccessToken")

    //토큰의 유효성 + 만료일자 확인
    @Throws(ExpiredJwtException::class, SignatureException::class)
    private fun validateToken(jwtToken: String?): Boolean {
        return try {
            val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken)
            !claims.body.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            throw ExpiredAccessTokenException("Token was expired!")
        } catch (e: SignatureException) {
            throw SignatureException("Token is not valid!")
        }
    }

    @Throws(ExpiredAccessTokenException::class, SignatureException::class)
    fun validateAccessToken(accessToken: String): Boolean {
        val userPk = getUserPk(accessToken)
        val redisRT: String = redisService.findAccessToken(userPk)
            ?: throw ExpiredAccessTokenException("Access token was expired. Please reissue.")
        if (redisRT != redisService.findAccessToken(userPk)) {
            redisService.deleteAccessToken(userPk)
            throw SignatureException("Token is not valid!")
        }
        return validateToken(accessToken)
    }

    @Throws(ExpiredRefreshTokenException::class, SignatureException::class)
    fun validateRefreshToken(refreshToken: String): Boolean {
        val userPk = getUserPk(refreshToken)
        val redisRT: String = redisService.findRefreshToken(userPk)
            ?: throw ExpiredRefreshTokenException("Refresh token was expired. Please re-login.")
        if (redisRT != redisService.findRefreshToken(userPk)) {
            redisService.deleteRefreshToken(userPk)
            throw SignatureException("Refresh Token is not valid!")
        }
        return validateToken(refreshToken)
    }

    @Throws(ReIssueBeforeAccessTokenExpiredException::class)
    fun reIssue(refreshToken: String): String {
        try {
            //refreshToken 자체가 잘못된 경우 (SignatureException인 경우?)
            val user_id = getUserPk(refreshToken)
            if (!checkAccessTokenIsExpiredInRedisWithUserPk(user_id)) {
                redisService.deleteAccessToken(user_id)
                redisService.deleteRefreshToken(user_id)
                throw ReIssueBeforeAccessTokenExpiredException("ReIssue before Access Token Expired !!!")
            }
            validateRefreshToken(refreshToken)
            return createAccessToken(user_id)
        } catch (e: SignatureException) {
            throw SignatureException("Refresh Token is not valid!")
        }
    }


    private fun checkAccessTokenIsExpiredInRedisWithUserPk(userPk: String): Boolean =
        redisService.findAccessToken(userPk) == null

    fun expireAllTokensWithUserPk(userPk: String): String {
        val accessToken = redisService.findAccessToken(userPk)
        val refreshToken = redisService.findRefreshToken(userPk)
        if (accessToken != null && refreshToken != null) {
            redisService.deleteAccessToken(userPk)
            redisService.deleteRefreshToken(userPk)
        }
        return userPk
    }
}