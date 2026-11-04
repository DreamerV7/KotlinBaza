
#!/bin/bash
set -e
cd "$(dirname "${BASH_SOURCE[0]}")/.."

case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
        CLASSPATH="out/app.jar;lib/kotlinx-cli-jvm-0.3.6.jar"
        ;;
    *)
        CLASSPATH="out/app.jar:lib/kotlinx-cli-jvm-0.3.6.jar"
        ;;
esac

java -cp "$CLASSPATH" MainKt "$@"
