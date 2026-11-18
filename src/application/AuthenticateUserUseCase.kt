
class AuthenticateUserUseCase(
    private val authenticator: Authenticator
) {
    fun execute(login: String, password: String): AccessResult {
        return authenticator.authenticate(login, password)
    }
}
