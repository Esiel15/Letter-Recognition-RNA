#!/bin/bash

echo "[i] Ejecutando"
java --add-opens=java.base/java.lang=ALL-UNNAMED -Xms2048M -Xmx8192M -cp ".;lib\weka.jar;bin" App 1 6
