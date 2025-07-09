#!/bin/bash
set -e

# Установка русской локали
apt-get update && apt-get install -y locales
echo "ru_RU.UTF-8 UTF-8" >> /etc/locale.gen
locale-gen ru_RU.UTF-8
update-locale LANG=ru_RU.UTF-8

# Создание БД advent с русской локалью
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE advent
    WITH ENCODING 'UTF8'
    LC_COLLATE 'ru_RU.UTF-8'
    LC_CTYPE 'ru_RU.UTF-8'
    TEMPLATE template0;
EOSQL