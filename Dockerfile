FROM eclipse-temurin:17-jre
WORKDIR /app
COPY out/app.jar out/app.jar
COPY lib/kotlinx-cli-jvm-0.3.6.jar lib/h2-2.5.252.jar lib/
COPY scripts/init.sql scripts/fill.sql scripts/
RUN java -cp lib/h2-2.5.252.jar org.h2.tools.RunScript -url jdbc:h2:./data/access -user sa -script scripts/init.sql \
    && java -cp lib/h2-2.5.252.jar org.h2.tools.RunScript -url jdbc:h2:./data/access -user sa -script scripts/fill.sql
ENTRYPOINT ["java", "-jar", "out/app.jar"]
