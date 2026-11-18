#!/usr/bin/env bash
set -euo pipefail
if [[ -z "${TELEGRAM_BOT_TOKEN:-}" || -z "${TELEGRAM_CHAT_ID:-}" ]]; then
    echo 'Telegram secrets are not configured' >&2
    exit 1
fi
curl --fail --silent --show-error --retry 2 \
    --request POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
    --data-urlencode "chat_id=${TELEGRAM_CHAT_ID}" \
    --data-urlencode "text=${GITHUB_REPOSITORY}: ${WORKFLOW_RESULT}. https://github.com/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}" \
    --output /dev/null
