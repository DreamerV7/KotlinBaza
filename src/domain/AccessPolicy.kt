class AccessPolicy(
    private val permissionRepository: PermissionRepository
) {
    fun hasPermission(
        login: String,
        resourcePath: String,
        action: String
    ): Boolean {
        val parts = resourcePath.split(".")
        val permissions = permissionRepository.findAll()

        for (i in parts.size downTo 1) {
            val parentPath = parts.take(i).joinToString(".")

            val permission = permissions.find {
                it.userLogin == login &&
                        it.resourcePath == parentPath &&
                        action in it.actions
            }

            if (permission != null) {
                return true
            }
        }

        return false
    }
}
