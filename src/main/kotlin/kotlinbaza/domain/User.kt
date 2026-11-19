package kotlinbaza

data class User(
    val login: String,
    val passwordHash: String,
    val salt: String
)
