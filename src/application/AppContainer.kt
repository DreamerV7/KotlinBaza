class AppContainer {
    private val userRepository = InMemoryUserRepository()
    private val resourceRepository = InMemoryResourceRepository()
    private val permissionRepository = InMemoryPermissionRepository()

    private val passwordHasher = Sha256PasswordHasher()

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
