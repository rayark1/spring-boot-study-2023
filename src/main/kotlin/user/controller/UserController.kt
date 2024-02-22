package com.wafflestudio.seminar.spring2023.user.controller

import com.wafflestudio.seminar.spring2023.user.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/api/v1/signup")
    fun signup(
        @RequestBody request: SignUpRequest,
    ): ResponseEntity<Unit> {
        val user = userService.signUp(
            username = request.username,
            password = request.password,
            image = request.image,
        )
        return ResponseEntity.ok(Unit)
    }

    @PostMapping("/api/v1/signin")
    fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> {
        val user = userService.signIn(
            username = request.username,
            password = request.password,
        )

        return ResponseEntity.ok(SignInResponse(
            accessToken = user.getAccessToken(),
        ))
    }

    @GetMapping("/api/v1/users/me")
    fun me(
        @RequestHeader(name = "Authorization", required = false) authorizationHeader: String?,
    ): ResponseEntity<UserMeResponse> {
        val accessToken = extractTokenFromHeader(authorizationHeader)
        val user = userService.authenticate(accessToken)
        return ResponseEntity.ok(UserMeResponse(
            username = user.username,
            image = user.image,
        ))
    }

    private fun extractTokenFromHeader(authorizationHeader: String?): String {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw AuthenticateException()
        }
        return authorizationHeader.removePrefix("Bearer ")
    }

    @ExceptionHandler
    fun handleException(e: UserException): ResponseEntity<Unit> {
        val status = when (e) {
            is SignUpBadUsernameException, is SignUpBadPasswordException -> 400
            is SignUpUsernameConflictException -> 409
            is SignInUserNotFoundException, is SignInInvalidPasswordException -> 404
            is AuthenticateException -> 401
        }
        return ResponseEntity.status(status).build()
    }
}

data class UserMeResponse(
    val username: String,
    val image: String,
)

data class SignUpRequest(
    val username: String,
    val password: String,
    val image: String,
)

data class SignInRequest(
    val username: String,
    val password: String,
)

data class SignInResponse(
    val accessToken: String,
)
