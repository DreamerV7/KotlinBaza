package kotlinbaza

import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

import java.sql.Connection

@Repository
class JdbcResourceRepository(@Lazy private val connection: Connection) : ResourceRepository {
    override fun findByPath(path: String): Resource? {
        return connection.prepareStatement(
            "SELECT path, max_volume FROM resources WHERE path = ?"
        ).use { statement ->
            statement.setString(1, path)
            statement.executeQuery().use { rows ->
                if (rows.next()) Resource(rows.getString("path"), rows.getInt("max_volume")) else null
            }
        }
    }
}
