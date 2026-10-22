
class Authenticator(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) {
    fun authenticate(login: String, password: String): AccessResult {
        val user = userRepository.findByLogin(login)
            ?: return AccessResult.INVALID_LOGIN

        val passwordHash = passwordHasher.hash(password, user.salt)

        if (passwordHash != user.passwordHash) {
            return AccessResult.INVALID_PASSWORD
        }

        return AccessResult.SUCCESS
    }
}
