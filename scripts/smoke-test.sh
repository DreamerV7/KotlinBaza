#!/bin/bash
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1
mkdir -p out
test_dir="$(mktemp -d ./out/db-smoke.XXXXXX)" || exit 1
export KOTLINBAZA_DB_PATH="$test_dir/access"
bash scripts/init-db.sh || exit 1

passed=0
total=13

check() {
    expected=$1
    description=$2
    shift 2

    output=$("$@" 2>&1)
    actual=$?

    if [ "$actual" -eq "$expected" ] && { [ "$expected" -eq 1 ] || [ -z "$output" ]; }; then
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
check 9 "Ошибка подключения (отсутствует файл БД), без вывода" \
    env KOTLINBAZA_DB_PATH="$test_dir/missing" bash scripts/run.sh --login alice --password qwerty --action read --resource A --volume 1

java -cp lib/h2-2.5.252.jar org.h2.tools.Shell -url "jdbc:h2:$test_dir/empty" -user sa -password '' -sql 'SELECT 1' >/dev/null || exit 1
check 10 "Ошибка SQL (отсутствует схема), без вывода" \
    env KOTLINBAZA_DB_PATH="$test_dir/empty" bash scripts/run.sh --login alice --password qwerty --action read --resource A --volume 1

java -cp lib/h2-2.5.252.jar org.h2.tools.Shell -url "jdbc:h2:$KOTLINBAZA_DB_PATH" -user sa -password '' -sql "UPDATE resources SET max_volume = 0 WHERE path = 'A.B.C'" >/dev/null || exit 1
check 8 "Изменение данных в БД влияет на следующий запуск" \
    bash scripts/run.sh --login alice --password qwerty --action read --resource A.B.C --volume 1

echo "Результат: $passed/$total"

[ "$passed" -eq "$total" ]
