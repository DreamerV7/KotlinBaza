# Зависимости тестов

JUnit Platform 1.12.2 / Jupiter 5.12.2. JAR-файлы загружены вручную из Maven Central, без систем сборки.

Standalone содержит API, engine, params и транзитивные зависимости. В IDEA и скриптах используется только standalone, чтобы избежать дублирования классов. Отдельные API/engine сохранены по ТЗ.

| Файл | Источник | SHA-256 |
|---|---|---|
| junit-jupiter-api-5.12.2.jar | [Maven Central](https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.12.2/junit-jupiter-api-5.12.2.jar) | `0b9ca728e4bcd9adc57f29deb9556ff9ed5e08b4e8843b875aba4e4e3e04f092` |
| junit-jupiter-engine-5.12.2.jar | [Maven Central](https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.12.2/junit-jupiter-engine-5.12.2.jar) | `f576c06b8accde9985063b8bc80526cb980eec24dee0b7221fc8ddd7ace65800` |
| junit-platform-console-standalone-1.12.2.jar | [Maven Central](https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.12.2/junit-platform-console-standalone-1.12.2.jar) | `329bd10288875a74d04c9ca6b7c9889c265ddf87b21a9ea42a7f7392f391472a` |

## JDBC-драйвер

H2 2.5.252 подключён вручную; используется через стандартный JDBC. Утилита RunScript из того же JAR нужна для инициализации.

| Файл | Источник | SHA-256 |
|---|---|---|
| h2-2.5.252.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/h2database/h2/2.5.252/h2-2.5.252.jar) | `90b11dc413da82070ef15fc943282c1408e6ffd34fa68998335fea8603b5ff20` |
