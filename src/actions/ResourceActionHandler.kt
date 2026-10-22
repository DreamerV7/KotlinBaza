
class ResourceActionHandler(
    override val name: String,
    private val resourceRepository: ResourceRepository,
    private val accessPolicy: AccessPolicy
) : ActionHandler {

    override fun execute(
        login: String,
        resource: String,
        volume: Int
    ): AccessResult {
        if (!ResourcePathValidator.isValid(resource) || volume < 0) {
            return AccessResult.INVALID_FORMAT
        }

        val resourceInfo = resourceRepository.findByPath(resource)
            ?: return AccessResult.RESOURCE_NOT_FOUND

        if (volume > resourceInfo.maxVolume) {
            return AccessResult.VOLUME_EXCEEDED
        }

        if (!accessPolicy.hasPermission(login, resource, name)) {
            return AccessResult.ACCESS_DENIED
        }

        return AccessResult.SUCCESS
    }
}
