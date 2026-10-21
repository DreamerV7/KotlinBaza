interface UserRepository {
    fun findByLogin(login: String): User?
}
