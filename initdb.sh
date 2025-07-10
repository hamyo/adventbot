#!/bin/bash
set -e

# Создание БД advent с русской локалью
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE advent
    WITH ENCODING 'UTF8'
    LC_COLLATE 'ru_RU.UTF-8'
    LC_CTYPE 'ru_RU.UTF-8'
    TEMPLATE template0;
EOSQL