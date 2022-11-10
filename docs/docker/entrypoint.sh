#!/bin/bash

if [ "$1" = 'serve' ]; then
    mkdocs serve -a "0.0.0.0:8000"
else
    mkdocs "$@"
fi
