package kotlinbaza

interface UserRepository {
    fun findByLogin(login: String): User?
}
