package kotlinbaza

import org.springframework.stereotype.Service


@Service
class AuthenticateUserUseCase(
    private val authenticator: Authenticator
) {
    fun execute(login: String, password: String): AccessResult {
        return authenticator.authenticate(login, password)
    }
}
