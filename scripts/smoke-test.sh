#!/bin/bash
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

passed=0
total=10

check() {
    expected=$1
    description=$2
    shift 2

    "$@" >/dev/null 2>&1
    actual=$?

    if [ "$actual" -eq "$expected" ]; then
        echo "OK   - $description"
        ((passed++))
    else
        echo "FAIL - $description (ожидался код $expected, получен $actual)"
    fi
}

check 0 "Успешный доступ" \
    bash scripts/run.sh --login alice --password qwerty --action read --resource A.B.C --volume 10

check 1 "Запрос справки --help" \
    bash scripts/run.sh --help

check 2 "Неверный пароль" \
    bash scripts/run.sh --login alice --password wrong --action read --resource A.B.C --volume 10

check 3 "Неверный логин" \
    bash scripts/run.sh --login nobody --password qwerty --action read --resource A.B.C --volume 10

check 4 "Неизвестное действие" \
    bash scripts/run.sh --login alice --password qwerty --action delete --resource A.B.C --volume 10

check 5 "Доступ запрещён" \
    bash scripts/run.sh --login alice --password qwerty --action execute --resource A.B.C --volume 10

check 6 "Ресурс не существует" \
    bash scripts/run.sh --login alice --password qwerty --action read --resource A.B.Z --volume 10

check 7 "Некорректный формат ресурса" \
    bash scripts/run.sh --login alice --password qwerty --action read --resource A-B --volume 10

check 8 "Объём превышает максимум" \
    bash scripts/run.sh --login alice --password qwerty --action read --resource A.B.C --volume 150

check 1 "Запрос справки -h" \
    bash scripts/run.sh -h

echo
echo "Результат: $passed/$total"

[ "$passed" -eq "$total" ]
