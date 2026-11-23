#!/usr/bin/env bash
echo "============================================================"
echo "  MALL OF TOMORROW - SMART INVENTORY DEMAND PREDICTOR"
echo "============================================================"
echo "Compiling Java project (Console + Swing GUI)..."

mkdir -p bin
javac -d bin $(find src -name "*.java")

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Compilation failed. Ensure JDK 8+ is installed."
    exit 1
fi

echo "Compilation successful!"
echo ""

if [ "$1" == "--console" ]; then
    echo "Starting Console Interface..."
    java -cp bin com.mall.Main "$@"
else
    echo "Starting Graphical Interface (Swing/AWT)..."
    echo "(Tip: Run './run.sh --console' to launch the terminal console version)"
    java -cp bin com.mall.ui.gui.GuiMain "$@"
fi
