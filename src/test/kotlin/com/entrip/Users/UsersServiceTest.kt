package com.entrip.Users

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.exception.FailToFindNicknameOrIdException
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.entrip.service.UsersService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.SignatureException
import java.util.*

class UsersServiceTest : BehaviorSpec() {

    val jwtTokenProvider = mockk<JwtTokenProvider>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val plannersRepository = mockk<PlannersRepository>()
    val usersRepository = mockk<UsersRepository>()
    val usersService = UsersService(usersRepository, plannersRepository, jwtTokenProvider, passwordEncoder)

    val users = Users(
        user_id = "test@gmail.com",
        nickname = "test",
        gender = 1,
        photoUrl = "test.com",
        token = "token",
        m_password = "testPassword"
    )

    init {
        given("UsersSaveRequestDto를 주고") {
            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = "test@gmail.com",
                nickname = "test",
                gender = 1,
                photoUrl = "test.com",
                password = "testPassword"
            )

            every { usersRepository.findAll() } returns emptyList()
            every { usersRepository.save(any()) } returns users
            every { passwordEncoder.encode(any()) } returns "testPassword"
            every { usersRepository.existsByUser_id(any()) } returns false

            `when`("save하면") {
                val result = usersService.save(usersSaveRequestDto)
                then("save된 user_id가 리턴된다") {
                    result shouldBe "test@gmail.com"
                }
            }
        }

        given("Users(test@gmail.com)가 Save된 상황에서") {
            val validUserId = "test@gmail.com"
            val invalidUserId = "invalid@gmail.com"
            val validNickname = "test"
            val invalidNickname = "invalid"
            val validPassword = "testPassword"
            val invalidPassword = "invalidPassword"
            val validRefreshToken = "refreshToken"
            val invalidRefreshToken = "invalidRefreshToken"
            val refreshedToken = "refreshedToken"

            every { usersRepository.findById(validUserId) } returns Optional.of(users)
            every { usersRepository.findById(invalidUserId) } returns Optional.empty()
            every { usersRepository.findAll() } returns listOf(users)
            every { usersRepository.existsByUser_id(validUserId) } returns true
            every { usersRepository.existsByUser_id(invalidUserId) } returns false
            every { usersRepository.existsByNickname(validNickname) } returns true
            every { usersRepository.existsByNickname(invalidNickname) } returns false
            every { jwtTokenProvider.createAccessToken(any()) } returns "accessToken"
            every { jwtTokenProvider.createRefreshToken(any()) } returns "refreshToken"
            every { passwordEncoder.matches(validPassword, any()) } returns true
            every { passwordEncoder.matches(invalidPassword, any()) } returns false
            every { jwtTokenProvider.getUserPk(validRefreshToken) } returns validUserId
            every { jwtTokenProvider.getUserPk(invalidRefreshToken) } throws SignatureException()
            every { jwtTokenProvider.reIssue(any()) } returns refreshedToken
            every { jwtTokenProvider.expireAllTokensWithUserPk(any()) } returns validUserId


            `when`("findByUserId($validUserId)를 호출하면") {
                val result = usersService.findByUserId(validUserId)
                then("save된 usersResponseDto가 리턴된다") {
                    result.user_id shouldBe validUserId
                    result.gender shouldBe 1
                    result.nickname shouldBe validNickname
                    result.photoUrl shouldBe "test.com"
                    result.token shouldNotBe ""
                }
            }

            `when`("isExistUserId(validUserId)을 호출하면") {
                val result = usersService.isExistUserId(validUserId)
                then("true가 리턴된다") {
                    result shouldBe true
                }
            }

            `when`("isExistUserId(invalidUserId)을 호출하면") {
                val result = usersService.isExistUserId(invalidUserId)
                then("false가 리턴된다") {
                    result shouldBe false
                }
            }

            `when`("isExistNickname(validNickname)을 호출하면") {
                val result = usersService.isExistNickname(validNickname)
                then("true가 리턴된다") {
                    result shouldBe true
                }
            }

            `when`("isExistNickname(invalidNickname)을 호출하면") {
                val result = usersService.isExistNickname("invalid")
                then("false가 리턴된다") {
                    result shouldBe false
                }
            }

            `when`("findUserWithNicknameOrUserId(validUserId)을 호출하면") {
                val result = usersService.findUserWithNicknameOrUserId(validUserId)
                then("validUserId 가 리턴된다") {
                    result shouldBe validUserId
                }
            }

            `when`("findUserWithNicknameOrUserId(validNickname)을 호출하면") {
                val result = usersService.findUserWithNicknameOrUserId(validNickname)
                then("validUserId 가 리턴된다") {
                    result shouldBe validUserId
                }
            }

            `when`("findUserWithNicknameOrUserId(invalidUserId)을 호출하면") {
                then("FailToFindNicknameOrIdException 이 throw 된다") {
                    shouldThrow<FailToFindNicknameOrIdException> {
                        usersService.findUserWithNicknameOrUserId(
                            invalidUserId
                        )
                    }
                }
            }

            `when`("findUserWithNicknameOrUserId(invalidNickname)을 호출하면") {
                then("FailToFindNicknameOrIdException 이 throw 된다") {
                    shouldThrow<FailToFindNicknameOrIdException> {
                        usersService.findUserWithNicknameOrUserId(
                            invalidNickname
                        )
                    }
                }
            }

            `when`("updateToken(validUserId, token)을 호출하면") {
                val result = usersService.updateToken(validUserId, "tokenValue")
                then("validUserId 가 리턴된다") {
                    result shouldBe validUserId
                }
            }

            `when`("updateToken(invalidUserId, token)을 호출하면") {
                then("IllegalArgumentException 이 throw 된다") {
                    shouldThrow<IllegalArgumentException> { usersService.updateToken(invalidUserId, "tokenValue") }
                }
            }

            `when`("login(usersLoginRequestDto)을 호출하면") {
                val usersLoginRequestDto = UsersLoginRequestDto(
                    user_id = validUserId,
                    password = validPassword
                )
                val result = usersService.login(usersLoginRequestDto)
                then("UsersLoginResReturnDto 가 리턴된다") {
                    result.user_id shouldBe validUserId
                    result.nickname shouldBe validNickname
                    result.accessToken shouldNotBe null
                    result.refreshToken shouldNotBe null
                }
            }

            `when`("login(usersLoginRequestDto with invalidUserId)을 호출하면") {
                val usersLoginRequestDto = UsersLoginRequestDto(
                    user_id = invalidUserId,
                    password = validPassword
                )
                then("NotAcceptedException 이 throw 된다") {
                    shouldThrow<NotAcceptedException> { usersService.login(usersLoginRequestDto) }
                }
            }

            `when`("login(usersLoginRequestDto with invalidPassword)을 호출하면") {
                val usersLoginRequestDto = UsersLoginRequestDto(
                    user_id = validUserId,
                    password = invalidPassword
                )
                then("NotAcceptedException 이 throw 된다") {
                    shouldThrow<NotAcceptedException> { usersService.login(usersLoginRequestDto) }
                }
            }

            `when`("reIssue(validRefreshToken) 을 호출하면") {
                val result = usersService.reIssue(validRefreshToken)
                then("새로운 accessToken 이 리턴된다") {
                    result shouldBe refreshedToken
                }
            }

            `when`("reIssue(invalidRefreshToken) 을 호출하면") {
                then("SignatureException 이 throw 된다") {
                    shouldThrow<SignatureException> { usersService.reIssue(invalidRefreshToken) }
                }
            }

            `when`("logout(validUserId) 을 호출하면") {
                val result = usersService.logout(validUserId)
                then("validUserId가 리턴된다") {
                    result shouldBe validUserId
                }
            }
        }

        given("planners 두 개가 저장된 상태에서") {
            val validUserId = "test@gmail.com"

            val planners1 = Planners(
                planner_id = 1,
                title = "title",
                start_date = "startDate",
                end_date = "endDate"
            )

            val planners2 = Planners(
                planner_id = 2,
                title = "title",
                start_date = "startDate",
                end_date = "endDate"
            )
            every { usersRepository.findById(validUserId) } returns Optional.of(users)
            every { plannersRepository.findById(1) } returns Optional.of(planners1)
            every { plannersRepository.findById(2) } returns Optional.of(planners2)


            `when` ("addPlanners (1, validUser_id) 를 호출했을 때") {
                val result = usersService.addPlanners(1, validUserId)
                then("planner_id(1) 가 리턴된다") {
                    result shouldBe 1
                }
                then("users의 planners size는 1이다.") {
                    users.planners.size shouldBe 1
                }
            }

            `when` ("addPlanners (2, validUser_id) 를 호출했을 때") {
                val result = usersService.addPlanners(2, validUserId)
                then("planner_id(2) 가 리턴된다") {
                    result shouldBe 2
                }
                then("users의 planners size는 2이다.") {
                    users.planners.size shouldBe 2
                }
            }

            `when` ("2개의 플래너를 add한 이후 findAllPlannersWithUserId(validUser_id)를 호출했을 때") {
                val result = usersService.findAllPlannersWithUserId(validUserId)
                then("2개의 plannersReturnDto가 들어있는 plannersList가 반환된다.") {
                    result.size shouldBe 2
                    result.get(0).planner_id shouldBe 1
                    result.get(1).planner_id shouldBe 2
                }
            }
        }
    }
}