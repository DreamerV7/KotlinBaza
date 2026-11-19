package kotlinbaza

import java.security.MessageDigest

@org.springframework.stereotype.Component
class Sha256PasswordHasher : PasswordHasher {
    override fun hash(password: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}
