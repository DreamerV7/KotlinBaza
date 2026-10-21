interface ActionHandler {
    val name: String

    fun execute(
        login: String,
        resource: String,
        volume: Int
    ): Int
}
