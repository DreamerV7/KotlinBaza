
class ActionRegistry(
    handlers: List<ActionHandler>
) {
    private val handlersByName = handlers.associateBy { it.name }

    fun execute(
        action: String,
        login: String,
        resource: String,
        volume: Int
    ): AccessResult {
        val handler = handlersByName[action]
            ?: return AccessResult.UNKNOWN_ACTION

        return handler.execute(login, resource, volume)
    }
}
