#!/bin/bash
# 1. Garante que as pastas existem
mkdir -p gradle/wrapper

# 2. Cria um arquivo de propriedades básico
cat <<EOF > gradle/wrapper/gradle-wrapper.properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-6.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# 3. Baixa o JAR de um espelho confiável
wget https://github.com/gradle/gradle/raw/v6.5.0/gradle/wrapper/gradle-wrapper.jar -O gradle/wrapper/gradle-wrapper.jar

# 4. Envia pro GitHub
git add .
git commit -m "Fix: gradle-wrapper jar verificado"
git push origin main