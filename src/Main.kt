
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlin.system.exitProcess

const val SUCCESS = 0
const val HELP = 1
const val INVALID_PASSWORD = 2
const val INVALID_LOGIN = 3
const val UNKNOWN_ACTION = 4
const val ACCESS_DENIED = 5
const val RESOURCE_NOT_FOUND = 6
const val INVALID_FORMAT = 7
const val VOLUME_EXCEEDED = 8

fun AccessResult.toExitCode(): Int = when (this) {
    AccessResult.SUCCESS -> SUCCESS
    AccessResult.INVALID_PASSWORD -> INVALID_PASSWORD
    AccessResult.INVALID_LOGIN -> INVALID_LOGIN
    AccessResult.UNKNOWN_ACTION -> UNKNOWN_ACTION
    AccessResult.ACCESS_DENIED -> ACCESS_DENIED
    AccessResult.RESOURCE_NOT_FOUND -> RESOURCE_NOT_FOUND
    AccessResult.INVALID_FORMAT -> INVALID_FORMAT
    AccessResult.VOLUME_EXCEEDED -> VOLUME_EXCEEDED
}

fun printHelp() {
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
}

fun main(args: Array<String>) {
    if (args.contains("-h") || args.contains("--help")) {
        printHelp()
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
        ArgType.String,
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
        printHelp()
        exitProcess(HELP)
    }

    parser.parse(args)

    val parsedVolume = volume.toIntOrNull()
        ?: exitProcess(INVALID_FORMAT)

    val app = AppContainer()

    val authResult = app.authenticateUser.execute(login, password)
    if (authResult != AccessResult.SUCCESS) {
        exitProcess(authResult.toExitCode())
    }

    val result = app.checkAccess.execute(
        action = action,
        login = login,
        resource = resource,
        volume = parsedVolume
    )

    exitProcess(result.toExitCode())
}
