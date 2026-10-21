class InMemoryUserRepository : UserRepository {

    private val users = listOf(
        User(
            login = "alice",
            passwordHash = "cc09a9884adc41d7d26fa39899c8e5da13330de003bcda215063c80730d6a16b",
            salt = "d05a3a4e-f6af-4436-976d-7e37094a9d7e"
        ),
        User(
            login = "bob",
            passwordHash = "d392273a5272991bffa517fcec7eab0a3656b72b9dbacd2094ac5593cde107f6",
            salt = "b3faf95a-568f-43fd-93f9-e60ecb6d4392"
        )
    )

    override fun findByLogin(login: String): User? {
        return users.find { it.login == login }
    }
}
