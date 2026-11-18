import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnectionException(cause: SQLException) : RuntimeException(cause)

object JdbcDatabase {
    fun connect(path: String = System.getenv("KOTLINBAZA_DB_PATH") ?: "./data/access"): Connection {
        try {
            return DriverManager.getConnection("jdbc:h2:$path;IFEXISTS=TRUE", "sa", "")
        } catch (exception: SQLException) {
            throw DatabaseConnectionException(exception)
        }
    }
}
