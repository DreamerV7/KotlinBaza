import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.sql.SQLException

class JdbcRepositoryTest : DatabaseTest() {
    @Test fun shouldReadUserAndRejectSqlInjection() {
        val repository = JdbcUserRepository(connection)
        val user = repository.findByLogin("alice")!!
        assertEquals("alice", user.login)
        assertEquals(user.passwordHash, Sha256PasswordHasher().hash("qwerty", user.salt))
        assertNull(repository.findByLogin("missing"))
        assertNull(repository.findByLogin("alice' OR '1'='1"))
    }

    @Test fun shouldReadResourceAndReturnNullForUnknownPath() {
        val repository = JdbcResourceRepository(connection)
        assertEquals(Resource("A.B.C", 100), repository.findByPath("A.B.C"))
        assertNull(repository.findByPath("A.B.Z"))
        assertNull(repository.findByPath("A' OR '1'='1"))
    }

    @Test fun shouldGroupMultipleActionsForSamePermission() {
        executeSql("INSERT INTO permissions VALUES ('alice', 'A', 'write')")
        val permission = JdbcPermissionRepository(connection).findAll().single { it.userLogin == "alice" && it.resourcePath == "A" }
        assertEquals(setOf("read", "write"), permission.actions)
    }

    @Test fun shouldUseModifiedResourcesAndPermissionsWithoutRebuilding() {
        executeSql("UPDATE resources SET max_volume = 0 WHERE path = 'A.B.C'")
        assertEquals(AccessResult.VOLUME_EXCEEDED, app.checkAccess.execute("read", "alice", "A.B.C", 1))
        executeSql("DELETE FROM permissions WHERE user_login = 'alice'")
        assertEquals(AccessResult.ACCESS_DENIED, app.checkAccess.execute("read", "alice", "A", 1))
    }

    @Test fun shouldPersistDataAfterClosingAndReopeningConnection() {
        executeSql("UPDATE resources SET max_volume = 42 WHERE path = 'A'")
        connection.close()
        assertTrue(Files.exists(directory.resolve("access.mv.db")))
        connection = JdbcDatabase.connect(databasePath)
        assertEquals(Resource("A", 42), JdbcResourceRepository(connection).findByPath("A"))
    }

    @Test fun shouldInitializeRepeatedlyWithoutOverwritingDataOrDuplicatingRows() {
        executeSql("UPDATE resources SET max_volume = 42 WHERE path = 'A'")
        runScript("scripts/init.sql")
        runScript("scripts/fill.sql")
        assertEquals(Resource("A", 42), JdbcResourceRepository(connection).findByPath("A"))
        assertEquals(3, JdbcPermissionRepository(connection).findAll().size)
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM resources").use { rows ->
                assertTrue(rows.next())
                assertEquals(7, rows.getInt(1))
            }
        }
    }

    @Test fun shouldRejectInvalidForeignKeysAndNegativeVolume() {
        assertThrows(SQLException::class.java) { executeSql("INSERT INTO permissions VALUES ('missing', 'A', 'read')") }
        assertThrows(SQLException::class.java) { executeSql("INSERT INTO permissions VALUES ('alice', 'missing', 'read')") }
        assertThrows(SQLException::class.java) { executeSql("UPDATE resources SET max_volume = -1 WHERE path = 'A'") }
    }

    @Test fun shouldReturnEmptyPermissionListWhenNoGrantsExist() {
        executeSql("DELETE FROM permissions")
        assertTrue(JdbcPermissionRepository(connection).findAll().isEmpty())
    }

    @Test fun shouldDistinguishConnectionFailureFromQueryFailure() {
        assertThrows(DatabaseConnectionException::class.java) { JdbcDatabase.connect(directory.resolve("missing").toString().replace('\\', '/')) }
        assertFalse(Files.exists(directory.resolve("missing.mv.db")))
        executeSql("DROP TABLE permissions")
        assertThrows(SQLException::class.java) { JdbcPermissionRepository(connection).findAll() }
    }
}
