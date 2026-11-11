#!/usr/bin/env bash
set -euo pipefail
image="${1:?Usage: bash scripts/docker-test.sh image}"
check() {
    local expected="$1" actual=0
    shift
    docker run --rm "$image" "$@" || actual=$?
    if [[ "$actual" != "$expected" ]]; then
        echo "Expected exit code $expected, received $actual" >&2
        exit 1
    fi
}
check 0 --login alice --password qwerty --action read --resource A.B.C --volume 10
check 5 --login alice --password qwerty --action execute --resource A.B.C --volume 10
check 1 --help
