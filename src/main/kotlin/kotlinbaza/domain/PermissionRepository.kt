package kotlinbaza

interface PermissionRepository {
    fun findAll(): List<Permission>
}
