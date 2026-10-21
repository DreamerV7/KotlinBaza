
class Authenticator(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) {
    fun authenticate(login: String, password: String): Int {
        val user = userRepository.findByLogin(login)
            ?: return INVALID_LOGIN

        val passwordHash = passwordHasher.hash(password, user.salt)

        if (passwordHash != user.passwordHash) {
            return INVALID_PASSWORD
        }

        return SUCCESS
    }
}
