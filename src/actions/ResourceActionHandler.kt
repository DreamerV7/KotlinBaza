class ResourceActionHandler(
    override val name: String,
    private val resourceRepository: ResourceRepository,
    private val accessPolicy: AccessPolicy
) : ActionHandler {

    override fun execute(
        login: String,
        resource: String,
        volume: Int
    ): Int {
        if (!ResourcePathValidator.isValid(resource) || volume < 0) {
            return INVALID_FORMAT
        }

        val resourceInfo = resourceRepository.findByPath(resource)
            ?: return RESOURCE_NOT_FOUND

        if (volume > resourceInfo.maxVolume) {
            return VOLUME_EXCEEDED
        }

        if (!accessPolicy.hasPermission(login, resource, name)) {
            return ACCESS_DENIED
        }

        return SUCCESS
    }
}
