# Миграция на Spring Boot и Maven

## Структура

Код перенесён в src/main/kotlin/kotlinbaza, тесты в src/test/kotlin/kotlinbaza, настройки в src/main/resources. Пакет kotlinbaza ограничивает сканирование компонентов приложения. Доменные модели и интерфейсы не зависят от Spring.

## Внедрение зависимостей

Authenticator, AuthenticateUserUseCase, CheckAccessUseCase и ActionRegistry зарегистрированы через @Service. JDBC-репозитории используют @Repository, хешер и CliRunner используют @Component. CliRunner принимает сервисы через конструктор с @Autowired и реализует CommandLineRunner.

ApplicationConfiguration создаёт через @Bean политику доступа, три обработчика и JDBC-соединение. Реестр получает список ActionHandler от Spring. Соединение ленивое, создаётся при первом обращении к репозиторию и закрывается контейнером. Прежний ручной AppContainer сохранён только в тестовых исходниках как средство сборки изолированных тестов.

Spring Boot запускается с web-application-type=none. Веб-зависимости, MVC, Security, REST и ORM не добавлены.

## Логирование

Logback настроен в logback-spring.xml. Единственный appender пишет в logs/kotlinbaza.log, используется ротация по размеру и дате. Banner и стартовая информация отключены. Сервис записывает только результат операции, без пароля и аргументов. Справка остаётся единственным выводом приложения в консоль.

## Maven и поставка

pom.xml описывает зависимости, Kotlin Spring compiler plugin, Spring Boot repackage, JUnit и JaCoCo. Maven Wrapper фиксирует Maven 3.9.11. Исполняемый target/kotlinbaza.jar содержит зависимости и запускается через java -jar.

Скрипты сборки, тестирования и покрытия используют Maven Wrapper. SQL и инициализация H2 сохранены. Docker и архив релиза используют собранный fat JAR. CI запускает Maven, JUnit, CLI и SonarCloud; CD использует артефакт того же проверенного коммита и проверяет контейнер до публикации.
