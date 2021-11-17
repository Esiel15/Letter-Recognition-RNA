#!/bin/bash

echo "[i] Compilando"
javac -cp ".;lib\weka.jar" -d bin src/**/*.java src/*.java

echo "[i] Ejecutando"
java --add-opens=java.base/java.lang=ALL-UNNAMED -Xms2048M -Xmx8192M -cp ".;lib\weka.jar;bin" App 2 5
