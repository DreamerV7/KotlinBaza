class InMemoryPermissionRepository : PermissionRepository {

    private val permissions = listOf(
        Permission("alice", "A", setOf("read")),
        Permission("alice", "A.B", setOf("write")),
        Permission("bob", "A.B.C", setOf("execute"))
    )

    override fun findAll(): List<Permission> {
        return permissions
    }
}
