FROM eclipse-temurin:17-jdk
LABEL authors="Danil"

# Установка дополнительных инструментов
RUN apt-get update && \
    apt-get install -y wget unzip && \
    rm -rf /var/lib/apt/lists/*

# Скачивание JDT LS
WORKDIR /opt/jdtls
ENV JDTLS_VERSION=1.57.0
ENV JDTLS_BUILD=202602261110

RUN wget https://download.eclipse.org/jdtls/snapshots/jdt-language-server-${JDTLS_VERSION}-${JDTLS_BUILD}.tar.gz && \
    tar -xzf jdt-language-server-${JDTLS_VERSION}-${JDTLS_BUILD}.tar.gz && \
    rm jdt-language-server-${JDTLS_VERSION}-${JDTLS_BUILD}.tar.gz

## Создание скрипта запуска
#RUN echo '#!/bin/bash\n\
#EQUINOX_JAR=$(find /opt/jdtls/plugins -name "org.eclipse.equinox.launcher_*.jar" | head -n 1)\n\
#java \\\n\
#    --add-modules=ALL-SYSTEM \\\n\
#    --add-opens java.base/java.util=ALL-UNNAMED \\\n\
#    --add-opens java.base/java.lang=ALL-UNNAMED \\\n\
#    -Declipse.application=org.eclipse.jdt.ls.core.id1 \\\n\
#    -Dosgi.bundles.defaultStartLevel=4 \\\n\
#    -Declipse.product=org.eclipse.jdt.ls.core.product \\\n\
#    -Dfile.encoding=UTF-8 \\\n\
#    -DwatchParentProcess=true \\\n\
#    -Xmx1G \\\n\
#    -XX:+UseG1GC \\\n\
#    -XX:+UseStringDeduplication \\\n\
#    -jar "$EQUINOX_JAR" \\\n\
#    -configuration /opt/jdtls/config_linux \\\n\
#    -data /tmp/jdtls_workspace\n\
#' > /usr/local/bin/jdtls && chmod +x /usr/local/bin/jdtls

# Точка входа для LSP режима
#ENTRYPOINT ["/usr/local/bin/jdtls"]