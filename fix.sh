#!/bin/bash

# 1. Limpar a sujidade local que está a causar erro
echo "🧹 Limpando arquivos corrompidos..."
rm -f gradlew
rm -f gradle/wrapper/gradle-wrapper.jar
rm -f gradle/wrapper/gradle-wrapper.properties

# 2. Criar o novo ficheiro de Workflow (O cérebro da operação)
# Este ficheiro diz ao GitHub para ele mesmo gerar o Gradle
echo "📝 Atualizando configuração do GitHub Actions..."
mkdir -p .github/workflows

cat <<EOF > .github/workflows/android.yml
name: Android CI

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: gradle

      - name: Regenerate Gradle Wrapper
        run: |
          # Aqui o GitHub gera o arquivo que estava dando erro no seu PC
          gradle wrapper --gradle-version 6.5

      - name: Build with Gradle
        run: |
          chmod +x gradlew
          ./gradlew assembleDebug
          
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: FileManager-ARMv7
          path: app/build/outputs/apk/debug/*.apk
EOF

# 3. Enviar para o GitHub
echo "🚀 Enviando correções..."
git add .
git commit -m "Fix: Deixando o GitHub gerar o Gradle Wrapper"
git push origin main

echo "✅ FEITO! Agora vá à aba 'Actions' no seu link:"
echo "https://github.com/salyan-scripts/FileManager/actions"