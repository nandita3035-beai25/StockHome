#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if command -v xdg-open > /dev/null; then
    xdg-open "$SCRIPT_DIR/index.html"
elif command -v open > /dev/null; then
    open "$SCRIPT_DIR/index.html"
fi
