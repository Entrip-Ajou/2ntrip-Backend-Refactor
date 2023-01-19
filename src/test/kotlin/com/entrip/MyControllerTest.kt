package com.entrip

import com.entrip.controller.UsersController
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.service.UsersService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MyControllerTest(@Autowired val mvc: MockMvc) {

    private val objectMapper = ObjectMapper()

    @MockBean
    lateinit var usersService: UsersService

    @Test
    fun users_회원가입_테스트() {
        val usersSaveRequestDto = UsersSaveRequestDto("testUser_id", "testUser_nickname", 0, "photoUrl")

        mvc.perform(
            MockMvcRequestBuilders.post("/api/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usersSaveRequestDto))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(usersSaveRequestDto)))

    }
}
