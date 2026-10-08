#!/bin/bash

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
    ./run.sh --login alice --password qwerty --action read --resource A.B.C --volume 10

check 1 "Запрос справки --help" \
    ./run.sh --help

check 2 "Неверный пароль" \
    ./run.sh --login alice --password wrong --action read --resource A.B.C --volume 10

check 3 "Неверный логин" \
    ./run.sh --login nobody --password qwerty --action read --resource A.B.C --volume 10

check 4 "Неизвестное действие" \
    ./run.sh --login alice --password qwerty --action delete --resource A.B.C --volume 10

check 5 "Доступ запрещён" \
    ./run.sh --login alice --password qwerty --action execute --resource A.B.C --volume 10

check 6 "Ресурс не существует" \
    ./run.sh --login alice --password qwerty --action read --resource A.B.Z --volume 10

check 7 "Некорректный формат ресурса" \
    ./run.sh --login alice --password qwerty --action read --resource A-B --volume 10

check 8 "Объём превышает максимум" \
    ./run.sh --login alice --password qwerty --action read --resource A.B.C --volume 150

check 1 "Запрос справки -h" \
    ./run.sh -h

echo
echo "Результат: $passed/$total"
