import org.h2.tools.RunScript
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

abstract class DatabaseTest {
    @TempDir lateinit var directory: Path
    protected lateinit var connection: Connection
    protected lateinit var databasePath: String
    protected val app: AppContainer
        get() = AppContainer(JdbcUserRepository(connection), JdbcResourceRepository(connection), JdbcPermissionRepository(connection), Sha256PasswordHasher())

    @BeforeEach fun createDatabase() {
        databasePath = directory.resolve("access").toAbsolutePath().toString().replace('\\', '/')
        connection = DriverManager.getConnection("jdbc:h2:$databasePath", "sa", "")
        runScript("scripts/init.sql")
        runScript("scripts/fill.sql")
    }

    protected fun runScript(file: String) {
        Files.newBufferedReader(Path.of(file)).use { reader -> RunScript.execute(connection, reader) }
    }

    protected fun executeSql(sql: String) {
        connection.createStatement().use { it.execute(sql) }
    }

    @AfterEach fun closeDatabase() {
        if (::connection.isInitialized) connection.close()
    }
}
