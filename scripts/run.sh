
#!/bin/bash
set -e
cd "$(dirname "${BASH_SOURCE[0]}")/.."

case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
        CLASSPATH="out/app.jar;lib/kotlinx-cli-jvm-0.3.6.jar;lib/h2-2.5.252.jar"
        ;;
    *)
        CLASSPATH="out/app.jar:lib/kotlinx-cli-jvm-0.3.6.jar:lib/h2-2.5.252.jar"
        ;;
esac

java_options=()
if [[ -n "${KOTLINBAZA_COVERAGE_FILE:-}" ]]; then
    java_options+=("-javaagent:lib/org.jacoco.agent-0.8.16-runtime.jar=destfile=$KOTLINBAZA_COVERAGE_FILE,append=true,excludes=kotlin.*:org.*:com.*")
fi
java "${java_options[@]}" -cp "$CLASSPATH" MainKt "$@"
