
class CheckAccessUseCase(
    private val actionRegistry: ActionRegistry
) {
    fun execute(
        action: String,
        login: String,
        resource: String,
        volume: Int
    ): AccessResult {
        return actionRegistry.execute(
            action = action,
            login = login,
            resource = resource,
            volume = volume
        )
    }
}
