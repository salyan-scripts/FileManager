#!/bin/bash

echo "☕ Atualizando para Java 17 e Gradle moderno..."

# 1. Atualizar o Workflow para Java 17
mkdir -p .github/workflows
cat <<EOF > .github/workflows/android.yml
name: Android CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Regenerate Gradle Wrapper
        run: |
          # Gera o wrapper compatível com Java 17
          gradle wrapper --gradle-version 8.5

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

# 2. Pequeno ajuste no build.gradle da raiz (necessário para Gradle 8+)
cat <<EOF > build.gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        # Versão do plugin compatível com Gradle 8 e Java 17
        classpath 'com.android.tools.build:gradle:8.1.0'
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
EOF

# 3. Enviar para o GitHub
git add .
git commit -m "Fix: Atualizando para Java 17 e Gradle 8.5"
git push origin main

echo "🚀 Pronto! Agora o GitHub vai usar o Java 17 e não deve mais reclamar da versão."