FROM eclipse-temurin:17-jdk
LABEL authors="Danil"

# Установка дополнительных инструментов
RUN apt-get update && \
    apt-get install -y wget unzip python3

# Скачивание JDT LS
WORKDIR /opt/jdtls

COPY jdt-language-server.tar.gz jdt.tar.gz
RUN tar -xzf jdt.tar.gz

WORKDIR /workspace

CMD [ "java", "-Declipse.application=org.eclipse.jdt.ls.core.id1", "-Dosgi.bundles.defaultStartLevel=4", "-Declipse.product=org.eclipse.jdt.ls.core.product", "-Dlog.level=ALL", "-noverify", "-Xmx1G", 	"--add-modules=ALL-SYSTEM", "--add-opens java.base/java.util=ALL-UNNAMED", "--add-opens java.base/java.lang=ALL-UNNAMED", "-jar /opt/jdtls/plugins/org.eclipse.equinox.launcher_*.jar", "-configuration /opt/jdtls/config_linux", "-data /workspace-info" ]