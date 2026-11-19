# Лабораторная работа №7

Климов Дмитрий, ПИ-241.

Консольная проверка доступа к ресурсам. Spring Boot используется для DI и запуска, без веб-сервера, MVC, Security и ORM. Данные хранятся в файловой H2 через JDBC.

## Требования и сборка

Нужен JDK 17 и интернет для первого скачивания Maven и зависимостей. Все зависимости и плагины объявлены в pom.xml. Wrapper фиксирует Maven 3.9.11.

```bash
bash mvnw clean install
bash scripts/build.sh
bash scripts/init-db.sh
bash scripts/run.sh --login alice --password qwerty --action read --resource A.B.C --volume 10
echo $?
```

В PowerShell можно использовать .\mvnw.cmd clean install. В IntelliJ IDEA откройте pom.xml как Maven-проект. Отдельный kotlinc и ручные JAR-файлы в lib не нужны.

Spring Boot собирает самодостаточный target/kotlinbaza.jar. Скрипт сборки также копирует его в out/app.jar для Docker и релизов.

```bash
java -jar target/kotlinbaza.jar --login alice --password qwerty --action read --resource A.B.C --volume 10
bash mvnw spring-boot:run -Dspring-boot.run.arguments="--help"
```

Команды Maven: clean, compile, test, package, install. Первый запуск spring-boot:run и JAR выполняется из корня проекта после инициализации БД.

## Архитектура

Исходники находятся в src/main/kotlin/kotlinbaza, тесты в src/test/kotlin/kotlinbaza, настройки в src/main/resources.

- domain содержит модели, интерфейсы репозиториев и правила доступа.
- application и auth содержат сервисы с внедрением зависимостей через конструкторы.
- infrastructure содержит JDBC-репозитории с @Repository и хешер с @Component.
- actions содержит обработчики и реестр действий.
- BootApplication запускает Spring. ApplicationConfiguration объявляет JDBC-соединение и обработчики через @Bean. CliRunner реализует CommandLineRunner.

Соединение создаётся лениво, поэтому справка доступна без БД. Spring закрывает соединение при закрытии контекста. JDBC statement и result set закрываются через use.

## База и коды завершения

bash scripts/init-db.sh создаёт data/access.mv.db из scripts/init.sql и scripts/fill.sql. Повторный запуск не перезаписывает существующие данные. Переменная KOTLINBAZA_DB_PATH задаёт другой файловый путь без расширения .mv.db.

| Код | Результат |
|---|---|
| 0 | Доступ разрешён |
| 1 | Справка или отсутствуют обязательные аргументы |
| 2 | Неверный пароль |
| 3 | Неизвестный пользователь |
| 4 | Неизвестное действие |
| 5 | Доступ запрещён |
| 6 | Ресурс не найден |
| 7 | Некорректный формат |
| 8 | Превышен объём |
| 9 | Ошибка подключения к БД |
| 10 | Ошибка SQL |

## Логи и тесты

Вывод приложения в консоль ограничен справкой. Banner Spring отключён. SLF4J и Logback пишут в logs/kotlinbaza.log с ротацией. Пароли и аргументы запроса в журнал не записываются.

```bash
bash scripts/test.sh
bash scripts/smoke-test.sh
bash scripts/coverage.sh
```

Перед отдельным smoke-запуском соберите приложение. coverage.sh выполняет сборку Maven, JUnit, CLI-проверки и формирует target/site/jacoco/jacoco.xml. CLI-тесты используют отдельную базу в out. Команды сборки Maven показывают свой технический вывод; собранное приложение работает без логов в консоли.

## Docker и CI/CD

Основная ветка main. CI запускается для PR в main, CD для push в main. Проверки того же коммита включают Maven, JUnit, CLI, покрытие и SonarCloud Quality Gate. CD проверяет контейнер на коды 0, 5, 1 до публикации. Образ и архив релиза содержат один проверенный JAR.

```bash
bash scripts/build.sh
docker build -t kotlinbaza:local .
bash scripts/docker-test.sh kotlinbaza:local
docker run --rm kotlinbaza:local --login alice --password qwerty --action read --resource A.B.C --volume 10
```

Docker Hub: https://hub.docker.com/r/skebobv7/kolek

Secrets: SONAR_TOKEN, DOCKERHUB_USERNAME, DOCKERHUB_TOKEN, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID. Variable DOCKERHUB_REPOSITORY: skebobv7/kolek. Уведомления отправляются в Telegram при успехе и ошибке. Архив out/release/kotlinbaza.tar.gz формируется scripts/package.sh и содержит fat JAR, JDBC-драйвер для инициализации и SQL-скрипты.
