import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplicationTest : DatabaseTest() {
    @Test fun shouldAuthenticateConfiguredUserThroughUseCase() {
        assertEquals(AccessResult.SUCCESS, app.authenticateUser.execute("alice", "qwerty"))
    }
    @Test fun shouldRejectWrongPasswordThroughUseCase() {
        assertEquals(AccessResult.INVALID_PASSWORD, app.authenticateUser.execute("alice", "wrong"))
    }
    @Test fun shouldRejectUnknownUserThroughUseCase() {
        assertEquals(AccessResult.INVALID_LOGIN, app.authenticateUser.execute("unknown", "qwerty"))
    }
    @Test fun shouldWireAllThreeActions() {
        assertEquals(AccessResult.SUCCESS, app.checkAccess.execute("read", "alice", "A.B.C", 10))
        assertEquals(AccessResult.SUCCESS, app.checkAccess.execute("write", "alice", "A.B.C", 10))
        assertEquals(AccessResult.SUCCESS, app.checkAccess.execute("execute", "bob", "A.B.C", 100))
        assertEquals(AccessResult.ACCESS_DENIED, app.checkAccess.execute("execute", "alice", "A.B.C", 10))
    }
    @Test fun shouldMapAllResultsToDocumentedExitCodes() {
        val expected = mapOf(
            AccessResult.SUCCESS to 0, AccessResult.INVALID_PASSWORD to 2,
            AccessResult.INVALID_LOGIN to 3, AccessResult.UNKNOWN_ACTION to 4,
            AccessResult.ACCESS_DENIED to 5, AccessResult.RESOURCE_NOT_FOUND to 6,
            AccessResult.INVALID_FORMAT to 7, AccessResult.VOLUME_EXCEEDED to 8
        )
        assertEquals(AccessResult.entries.toSet(), expected.keys)
        expected.forEach { (result, code) -> assertEquals(code, result.toExitCode()) }
    }
}
