package com.entrip.Users

import com.entrip.domain.entity.Users
import com.entrip.repository.UsersRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsersRepositoryTest : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var usersRepository: UsersRepository

    override fun beforeTest(testCase: TestCase) {
        usersRepository.deleteAll()
    }

    init {
        given("Users") {
            val users: Users = Users(
                user_id = "test@gmail.com",
                nickname = "test",
                gender = 1,
                photoUrl = "test.com",
                token = "token",
                m_password = "password"
            )
            `when`("Users를 저장하면") {
                usersRepository.save(users)
                val savedUsers = usersRepository.findAll().get(0)
                then("Users가 저장된다") {
                    savedUsers.user_id shouldBe users.user_id
                    savedUsers.nickname shouldBe users.nickname
                    savedUsers.gender shouldBe users.gender
                    savedUsers.photoUrl shouldBe users.photoUrl
                    savedUsers.token shouldBe users.token
                    savedUsers.m_password shouldBe users.m_password
                }
            }
            `when`("Users를 저장한 뒤 findById를 하면") {
                usersRepository.save(users)
                val targetUsers = usersRepository.findById(users.user_id!!)
                    .orElseThrow { IllegalArgumentException("Cannot Find Users with user_id") }
                then("Users가 조회된다") {
                    targetUsers.user_id shouldBe users.user_id
                    targetUsers.nickname shouldBe users.nickname
                    targetUsers.gender shouldBe users.gender
                    targetUsers.photoUrl shouldBe users.photoUrl
                    targetUsers.token shouldBe users.token
                    targetUsers.m_password shouldBe users.m_password
                }
            }
        }
    }
}
