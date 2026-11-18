import java.sql.Connection

class JdbcPermissionRepository(private val connection: Connection) : PermissionRepository {
    override fun findAll(): List<Permission> {
        return connection.prepareStatement(
            "SELECT user_login, resource_path, action FROM permissions ORDER BY user_login, resource_path, action"
        ).use { statement ->
            statement.executeQuery().use { rows ->
                val actions = linkedMapOf<Pair<String, String>, MutableSet<String>>()
                while (rows.next()) {
                    val key = rows.getString("user_login") to rows.getString("resource_path")
                    actions.getOrPut(key) { linkedSetOf() }.add(rows.getString("action"))
                }
                actions.map { (key, values) -> Permission(key.first, key.second, values) }
            }
        }
    }
}
