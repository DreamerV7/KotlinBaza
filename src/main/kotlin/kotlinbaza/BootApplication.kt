package kotlinbaza

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import java.sql.Connection
import java.sql.SQLException
import kotlin.system.exitProcess

@SpringBootApplication
class BootApplication

@Configuration
class ApplicationConfiguration {
    @Bean @Lazy
    fun connection(): Connection = JdbcDatabase.connect()
    @Bean fun accessPolicy(repository: PermissionRepository) = AccessPolicy(repository)
    @Bean fun readHandler(repository: ResourceRepository, policy: AccessPolicy): ActionHandler = ResourceActionHandler("read", repository, policy)
    @Bean fun writeHandler(repository: ResourceRepository, policy: AccessPolicy): ActionHandler = ResourceActionHandler("write", repository, policy)
    @Bean fun executeHandler(repository: ResourceRepository, policy: AccessPolicy): ActionHandler = ResourceActionHandler("execute", repository, policy)
}

@Component
class CliRunner @Autowired constructor(
    private val authenticateUser: AuthenticateUserUseCase,
    private val checkAccess: CheckAccessUseCase
) : CommandLineRunner {
    final var exitCode: Int = SUCCESS
        private set

    override fun run(vararg args: String) {
        exitCode = try {
            executeCli(arrayOf(*args), authenticateUser, checkAccess)
        } catch (exception: Exception) {
            val causes = generateSequence<Throwable>(exception) { it.cause }.toList()
            when {
                causes.any { it is DatabaseConnectionException } -> DATABASE_CONNECTION_ERROR
                causes.any { it is SQLException } -> SQL_QUERY_ERROR
                exception is IllegalArgumentException || exception is IllegalStateException -> INVALID_FORMAT
                else -> throw exception
            }
        }
        LoggerFactory.getLogger(javaClass).info("Request completed with status {}", exitCode)
    }
}

fun main(args: Array<String>) {
    val application = SpringApplication(BootApplication::class.java)
    application.setAddCommandLineProperties(false)
    val code = try {
        application.run(*args).use { it.getBean(CliRunner::class.java).exitCode }
    } catch (exception: Exception) {
        LoggerFactory.getLogger(BootApplication::class.java).error("Application startup failed", exception)
        SQL_QUERY_ERROR
    }
    exitProcess(code)
}
