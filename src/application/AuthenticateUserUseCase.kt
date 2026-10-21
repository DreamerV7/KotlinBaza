class AuthenticateUserUseCase(
    private val authenticator: Authenticator
) {
    fun execute(login: String, password: String): Int {
        return authenticator.authenticate(login, password)
    }
}
