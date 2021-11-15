#!/bin/bash

echo "[i] Compilando"
javac -cp ".;lib\weka.jar" -d bin src/**/*.java

echo "[i] Ejecutando"
java --add-opens=java.base/java.lang=ALL-UNNAMED -cp ".;lib\weka.jar;bin" App