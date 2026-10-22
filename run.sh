
#!/bin/bash
set -e

case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
        CLASSPATH="app.jar;lib/kotlinx-cli-jvm-0.3.6.jar"
        ;;
    *)
        CLASSPATH="app.jar:lib/kotlinx-cli-jvm-0.3.6.jar"
        ;;
esac

java -cp "$CLASSPATH" MainKt "$@"
