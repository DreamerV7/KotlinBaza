class InMemoryResourceRepository : ResourceRepository {

    private val resources = listOf(
        Resource("A", 1000),
        Resource("A.B", 500),
        Resource("A.B.C", 100),
        Resource("A.B.D", 200),
        Resource("A.X", 300),
        Resource("B", 800),
        Resource("B.C", 150)
    )

    override fun findByPath(path: String): Resource? {
        return resources.find { it.path == path }
    }
}
