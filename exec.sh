#!/bin/bash

echo "[i] Compilando"
javac -cp ".;lib\weka.jar" -d bin src/**/*.java src/*.java

echo "[i] Ejecutando"
<<<<<<< HEAD
java --add-opens=java.base/java.lang=ALL-UNNAMED -Xms2048M -Xmx8192M -cp ".;lib\weka.jar;bin" App 1 4
=======
java --add-opens=java.base/java.lang=ALL-UNNAMED -Xms2048M -Xmx8192M -cp ".;lib\weka.jar;bin" App 2 4
>>>>>>> 989a9ce53976370a7a12f4bc71ff3fe28950fce6
