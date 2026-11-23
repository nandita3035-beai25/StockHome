#!/usr/bin/env bash
echo "Opening StockHome Webapp in default browser..."
if which xdg-open > /dev/null; then
  xdg-open index.html
elif which open > /dev/null; then
  open index.html
else
  echo "Please open index.html in your web browser."
fi
