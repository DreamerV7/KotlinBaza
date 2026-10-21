interface ResourceRepository {
    fun findByPath(path: String): Resource?
}
