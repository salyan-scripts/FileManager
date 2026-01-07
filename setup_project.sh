#!/bin/bash

# Configurações do Projeto
PACKAGE_NAME="com.salyan.filemanager"
MIN_SDK=24
TARGET_SDK=34
BUILD_TOOLS="30.0.3"

echo "🚀 Iniciando a reconstrução do projeto FileManager..."

# 1. Limpeza total
rm -rf app gradle .github build.gradle settings.gradle gradlew* local.properties

# 2. Criando estrutura de pastas
mkdir -p .github/workflows
mkdir -p app/src/main/java/com/salyan/filemanager
mkdir -p app/src/main/res/layout
mkdir -p gradle/wrapper

# 3. Criando o build.gradle (Raiz)
cat <<EOF > build.gradle
buildscript {
    repositories { google(); mavenCentral() }
    dependencies { classpath 'com.android.tools.build:gradle:4.2.2' }
}
allprojects {
    repositories { google(); mavenCentral() }
}
EOF

# 4. Criando o settings.gradle
echo "include ':app'" > settings.gradle

# 5. Criando o build.gradle (App) - AQUI ESTÁ O SEGREDO DO ARMv7
cat <<EOF > app/build.gradle
apply plugin: 'com.android.application'

android {
    compileSdkVersion $TARGET_SDK
    buildToolsVersion "$BUILD_TOOLS"

    defaultConfig {
        applicationId "$PACKAGE_NAME"
        minSdkVersion $MIN_SDK
        targetSdkVersion $TARGET_SDK
        versionCode 1
        versionName "1.0"

        ndk {
            abiFilters "armeabi-v7a"
        }
    }
}
EOF

# 6. Criando o Manifesto
cat <<EOF > app/src/main/AndroidManifest.xml
<manifest xmlns:android="http://schemas.microsoft.com/apk/res/android" package="$PACKAGE_NAME">
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <application android:allowBackup="true" android:label="Salyan FM">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# 7. Criando uma Activity básica (Java)
cat <<EOF > app/src/main/java/com/salyan/filemanager/MainActivity.java
package com.salyan.filemanager;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Gerenciador de Arquivos ARMv7 Carregado!");
        setContentView(tv);
    }
}
EOF

# 8. Criando o Workflow do GitHub Actions (Para compilar online)
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

# 9. Baixando o Gradle Wrapper oficial (Essencial para o GitHub)
# Como você está no 32 bits, apenas criaremos um gradlew genérico que o GitHub (64 bits) usará
wget https://raw.githubusercontent.com/gradle/gradle/master/gradlew
chmod +x gradlew

echo "✅ Projeto estruturado! Agora vamos enviar para o GitHub..."

# 10. Comandos Git para subir tudo
git add .
git commit -m "Reconstrução total do projeto para ARMv7 via script"
git push origin main --force

echo "🔥 TUDO PRONTO! Vá na aba ACTIONS do seu GitHub e espere o APK ser gerado."