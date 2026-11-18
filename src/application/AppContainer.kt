class AppContainer(
    userRepository: UserRepository,
    resourceRepository: ResourceRepository,
    permissionRepository: PermissionRepository,
    passwordHasher: PasswordHasher
) {
    private val authenticator = Authenticator(
        userRepository,
        passwordHasher
    )

    val authenticateUser = AuthenticateUserUseCase(authenticator)

    private val accessPolicy = AccessPolicy(permissionRepository)

    private val actionRegistry = ActionRegistry(
        listOf(
            ResourceActionHandler("read", resourceRepository, accessPolicy),
            ResourceActionHandler("write", resourceRepository, accessPolicy),
            ResourceActionHandler("execute", resourceRepository, accessPolicy)
        )
    )

    val checkAccess = CheckAccessUseCase(actionRegistry)
}
