import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlin.system.exitProcess
import java.security.MessageDigest

const val SUCCESS = 0
const val HELP = 1
const val INVALID_PASSWORD = 2
const val INVALID_LOGIN = 3
const val UNKNOWN_ACTION = 4
const val ACCESS_DENIED = 5
const val RESOURCE_NOT_FOUND = 6
const val INVALID_FORMAT = 7
const val VOLUME_EXCEEDED = 8

fun hashPassword(password: String, salt: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest((salt + password).toByteArray())

    return bytes.joinToString("") { "%02x".format(it) }
}

data class User(
    val login: String,
    val passwordHash: String,
    val salt: String
)

val users = listOf(
    User(
        login = "alice",
        passwordHash = "cc09a9884adc41d7d26fa39899c8e5da13330de003bcda215063c80730d6a16b",
        salt = "d05a3a4e-f6af-4436-976d-7e37094a9d7e"
    ),
    User(
        login = "bob",
        passwordHash = "d392273a5272991bffa517fcec7eab0a3656b72b9dbacd2094ac5593cde107f6",
        salt = "b3faf95a-568f-43fd-93f9-e60ecb6d4392"
    )
)

data class Resource(
    val path: String,
    val maxVolume: Int
)

val resources = listOf(
    Resource("A", 1000),
    Resource("A.B", 500),
    Resource("A.B.C", 100),
    Resource("A.B.D", 200),
    Resource("A.X", 300),
    Resource("B", 800),
    Resource("B.C", 150)
)

data class Permission(
    val userLogin: String,
    val resourcePath: String,
    val actions: Set<String>
)

val permissions = listOf(
    Permission("alice", "A", setOf("read")),
    Permission("alice", "A.B", setOf("write")),
    Permission("bob", "A.B.C", setOf("execute"))
)

fun hasPermission(
    login: String,
    resourcePath: String,
    action: String
): Boolean {
    val parts = resourcePath.split(".")

    for (i in parts.size downTo 1) {
        val parentPath = parts.take(i).joinToString(".")

        val permission = permissions.find {
            it.userLogin == login &&
                    it.resourcePath == parentPath &&
                    action in it.actions
        }

        if (permission != null) {
            return true
        }
    }

    return false
}

fun isValidResourceFormat(resource: String): Boolean {
    if (resource.isEmpty()) {
        return false
    }

    val parts = resource.split(".")

    return parts.all { part ->
        part.length in 1..20 &&
                part.matches(Regex("[A-Za-z0-9_]+"))
    }
}

fun main(args: Array<String>) {
    if (args.contains("-h") || args.contains("--help")) {
        println(
            """
        Использование:
          --login <логин>          Логин пользователя
          --password <пароль>      Пароль пользователя
          --action <действие>      read, write или execute
          --resource <путь>        Путь к ресурсу
          --volume <объём>         Запрашиваемый объём

        Дополнительно:
          -h, --help               Показать справку
        """.trimIndent()
        )

        exitProcess(HELP)
    }
    val parser = ArgParser("lab1")

    val login by parser.option(
        ArgType.String,
        fullName = "login"
    ).required()

    val password by parser.option(
        ArgType.String,
        fullName = "password"
    ).required()

    val action by parser.option(
        ArgType.String,
        fullName = "action"
    ).required()

    val resource by parser.option(
        ArgType.String,
        fullName = "resource"
    ).required()

    val volume by parser.option(
        ArgType.Int,
        fullName = "volume"
    ).required()

    val requiredArguments = listOf(
        "--login",
        "--password",
        "--action",
        "--resource",
        "--volume"
    )

    if (requiredArguments.any { it !in args }) {
        println(
            """
        Использование:
          --login <логин>          Логин пользователя
          --password <пароль>      Пароль пользователя
          --action <действие>      read, write или execute
          --resource <путь>        Путь к ресурсу
          --volume <объём>         Запрашиваемый объём

        Дополнительно:
          -h, --help               Показать справку
        """.trimIndent()
        )

        exitProcess(HELP)
    }

    parser.parse(args)

    val user = users.find { it.login == login }

    if (user == null) {
        exitProcess(INVALID_LOGIN)
    }

    val passwordHash = hashPassword(password, user.salt)

    if (passwordHash != user.passwordHash) {
        exitProcess(INVALID_PASSWORD)
    }

    when (action) {
        "read", "write", "execute" -> {
            if (!isValidResourceFormat(resource) || volume < 0) {
                exitProcess(INVALID_FORMAT)
            }

            val resourceInfo = resources.find { it.path == resource }

            if (resourceInfo == null) {
                exitProcess(RESOURCE_NOT_FOUND)
            }

            if (volume > resourceInfo.maxVolume) {
                exitProcess(VOLUME_EXCEEDED)
            }
            if (!hasPermission(login, resource, action)) {
                exitProcess(ACCESS_DENIED)
            }

            exitProcess(SUCCESS)
        }

        else -> {
            exitProcess(UNKNOWN_ACTION)
        }
    }
}