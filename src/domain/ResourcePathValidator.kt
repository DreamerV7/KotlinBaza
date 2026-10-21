object ResourcePathValidator {
    fun isValid(resource: String): Boolean {
        if (resource.isEmpty()) {
            return false
        }

        val parts = resource.split(".")

        return parts.all { part ->
            part.length in 1..20 &&
                    part.matches(Regex("[A-Za-z0-9_]+"))
        }
    }
}
