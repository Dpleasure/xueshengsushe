#!/bin/bash
echo "启动后端SpringBoot应用..."
cd "$(dirname "$0")/backend"
if [ -f ".env.local" ]; then
  set -a
  . ./.env.local
  set +a
fi
mvn spring-boot:run





