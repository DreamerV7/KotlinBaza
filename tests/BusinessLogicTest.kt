import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ResourcePathValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = ["A", "A.B.C", "abc_123.X9", "123", "abcdefghijklmnopqrst"])
    fun shouldAcceptValidPaths(path: String) {
        assertTrue(ResourcePathValidator.isValid(path))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", ".A", "A.", "A..B", "A-B", "A B", "А", "A/B", "abcdefghijklmnopqrstu", "A.abcdefghijklmnopqrstu", "A\n"])
    fun shouldRejectInvalidPaths(path: String) {
        assertFalse(ResourcePathValidator.isValid(path))
    }
}

class AuthenticatorTest {
    private val user = User("alice", "stored-hash", "user-salt")
    private fun repository() = object : UserRepository {
        override fun findByLogin(login: String) = user.takeIf { it.login == login }
    }

    @Test
    fun shouldRejectUnknownLoginWithoutHashingPassword() {
        val hasher = object : PasswordHasher {
            override fun hash(password: String, salt: String): String = error("Must not hash for an unknown user")
        }
        assertEquals(AccessResult.INVALID_LOGIN, Authenticator(repository(), hasher).authenticate("missing", "password"))
    }

    @Test
    fun shouldHashSuppliedPasswordWithStoredSalt() {
        var calls = 0
        val hasher = object : PasswordHasher {
            override fun hash(password: String, salt: String): String {
                calls++
                assertEquals("supplied-password", password)
                assertEquals("user-salt", salt)
                return "stored-hash"
            }
        }
        assertEquals(AccessResult.SUCCESS, Authenticator(repository(), hasher).authenticate("alice", "supplied-password"))
        assertEquals(1, calls)
    }

    @Test
    fun shouldRejectIncorrectPasswordHash() {
        val hasher = object : PasswordHasher {
            override fun hash(password: String, salt: String) = "different-hash"
        }
        assertEquals(AccessResult.INVALID_PASSWORD, Authenticator(repository(), hasher).authenticate("alice", "wrong"))
    }
}

class AccessPolicyTest {
    private val policy = AccessPolicy(object : PermissionRepository {
        override fun findAll() = listOf(
            Permission("alice", "A.B", setOf("read", "write")),
            Permission("bob", "X", setOf("execute"))
        )
    })

    @Test fun shouldAllowExactPermission() = assertTrue(policy.hasPermission("alice", "A.B", "write"))
    @Test fun shouldInheritPermissionFromAncestor() = assertTrue(policy.hasPermission("alice", "A.B.C.D", "read"))
    @Test fun shouldNotGrantPermissionToParent() = assertFalse(policy.hasPermission("alice", "A", "read"))
    @Test fun shouldNotMatchPartialSegment() = assertFalse(policy.hasPermission("alice", "A.BC", "read"))
    @Test fun shouldNotGrantPermissionToSibling() = assertFalse(policy.hasPermission("alice", "A.C", "read"))
    @Test fun shouldNotUseAnotherUsersPermissions() = assertFalse(policy.hasPermission("bob", "A.B", "read"))
    @Test fun shouldRejectActionWithoutPermission() = assertFalse(policy.hasPermission("alice", "A.B", "execute"))
    @Test fun shouldDenyWhenNoPermissionsExist() {
        val empty = AccessPolicy(object : PermissionRepository {
            override fun findAll() = emptyList<Permission>()
        })
        assertFalse(empty.hasPermission("alice", "A", "read"))
    }
}

class ResourceActionHandlerTest {
    private fun handler(exists: Boolean = true, allowed: Boolean = true) = ResourceActionHandler(
        "write",
        object : ResourceRepository {
            override fun findByPath(path: String): Resource? {
                assertEquals("A.B", path)
                return if (exists) Resource(path, 100) else null
            }
        },
        AccessPolicy(object : PermissionRepository {
            override fun findAll() = if (allowed) listOf(Permission("alice", "A", setOf("write"))) else emptyList()
        })
    )

    @Test fun shouldRejectInvalidPathBeforeRepositoryLookup() {
        assertEquals(AccessResult.INVALID_FORMAT, handler().execute("alice", "A-B", 0))
    }
    @Test fun shouldRejectNegativeVolume() {
        assertEquals(AccessResult.INVALID_FORMAT, handler().execute("alice", "A.B", -1))
    }
    @Test fun shouldReportMissingResourceBeforeCheckingPermission() {
        assertEquals(AccessResult.RESOURCE_NOT_FOUND, handler(exists = false, allowed = false).execute("alice", "A.B", 101))
    }
    @Test fun shouldReportExceededVolumeBeforeCheckingPermission() {
        assertEquals(AccessResult.VOLUME_EXCEEDED, handler(allowed = false).execute("alice", "A.B", 101))
    }
    @Test fun shouldDenyAccessWithoutPermission() {
        assertEquals(AccessResult.ACCESS_DENIED, handler(allowed = false).execute("alice", "A.B", 10))
    }
    @Test fun shouldCheckHandlerActionAndLogin() {
        assertEquals(AccessResult.ACCESS_DENIED, handler().execute("bob", "A.B", 10))
        val readOnly = ResourceActionHandler("write", object : ResourceRepository {
            override fun findByPath(path: String) = Resource(path, 100)
        }, AccessPolicy(object : PermissionRepository {
            override fun findAll() = listOf(Permission("alice", "A", setOf("read")))
        }))
        assertEquals(AccessResult.ACCESS_DENIED, readOnly.execute("alice", "A.B", 10))
    }
    @ParameterizedTest
    @ValueSource(ints = [0, 1, 99, 100])
    fun shouldAllowVolumeWithinInclusiveLimits(volume: Int) {
        assertEquals(AccessResult.SUCCESS, handler().execute("alice", "A.B", volume))
    }
}

class ActionRegistryTest {
    @Test fun shouldRejectUnknownActionWithoutCallingHandler() {
        val registry = ActionRegistry(listOf(object : ActionHandler {
            override val name = "read"
            override fun execute(login: String, resource: String, volume: Int): AccessResult = error("Must not dispatch")
        }))
        assertEquals(AccessResult.UNKNOWN_ACTION, registry.execute("delete", "alice", "A", 1))
    }
    @Test fun shouldDispatchArgumentsAndReturnHandlerResult() {
        var calls = 0
        val registry = ActionRegistry(listOf(object : ActionHandler {
            override val name = "write"
            override fun execute(login: String, resource: String, volume: Int): AccessResult {
                calls++
                assertEquals("alice", login)
                assertEquals("A.B", resource)
                assertEquals(42, volume)
                return AccessResult.VOLUME_EXCEEDED
            }
        }))
        assertEquals(AccessResult.VOLUME_EXCEEDED, CheckAccessUseCase(registry).execute("write", "alice", "A.B", 42))
        assertEquals(1, calls)
    }
}

class Sha256PasswordHasherTest {
    @Test fun shouldMatchKnownSha256VectorWithSaltBeforePassword() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256PasswordHasher().hash("bc", "a"))
    }
    @Test fun shouldHashEmptyInput() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Sha256PasswordHasher().hash("", ""))
    }
    @Test fun shouldChangeHashWhenSaltChanges() {
        assertNotEquals(Sha256PasswordHasher().hash("password", "salt1"), Sha256PasswordHasher().hash("password", "salt2"))
    }
}
