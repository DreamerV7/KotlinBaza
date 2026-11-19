package kotlinbaza

import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

import java.sql.Connection

@Repository
class JdbcUserRepository(@Lazy private val connection: Connection) : UserRepository {
    override fun findByLogin(login: String): User? {
        return connection.prepareStatement(
            "SELECT login, password_hash, salt FROM app_users WHERE login = ?"
        ).use { statement ->
            statement.setString(1, login)
            statement.executeQuery().use { rows ->
                if (rows.next()) User(rows.getString("login"), rows.getString("password_hash"), rows.getString("salt"))
                else null
            }
        }
    }
}
