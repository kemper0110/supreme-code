# Модули plugin-sdk, common-plugins, test-runner

## 1. plugin-sdk

### Назначение

Библиотека, предоставляющая API для создания плагинов тестирования кода на различных языках программирования. Модуль содержит интерфейс `LanguageTester` и инструменты для парсинга отчётов о тестировании в формате JUnit XML.

### Структура модуля

```
plugin-sdk/
├── src/main/java/org/supremecode/pluginsdk/
│   ├── LanguageTester.java          # Главный интерфейс плагина
│   ├── JunitParser.java             # Парсер JUnit XML отчётов
│   ├── result/
│   │   ├── TestExecutionResult.java # DTO с результатами прогона
│   │   ├── TestCase.java            # DTO с информацией о тест-кейсе
│   │   └── Summary.java             # Пустая заглушка
│   └── junit/                       # JAXB классы (сгенерированы из XSD)
│       ├── Testsuites.java
│       ├── Testsuite.java
│       ├── Testcase.java
│       ├── Failure.java
│       ├── Error.java
│       ├── Properties.java
│       ├── Property.java
│       └── ObjectFactory.java
└── pom.xml
```

### Интерфейс LanguageTester

Центральный элемент SDK определяет контракт для всех плагинов тестирования:

```java
public interface LanguageTester {
    String languageId();           // Идентификатор языка (java, cpp, javascript)
    String imageName();            // Название Docker-образа для запуска тестов
    String testsPath();            // Путь к файлу с тестами внутри контейнера
    String solutionPath();         // Путь к файлу с решением внутри контейнера
    String reportPath();           // Путь к файлу с JUnit отчётом
    TestExecutionResult verdict(String report, int exitCode); // Анализ результата
}
```

### Парсер JUnit XML (JunitParser)

Реализует unmarshalling XML-отчётов стандарта JUnit в объектную модель:

```java
public class JunitParser {
    protected JAXBContext jaxbContext;

    public Testsuite parseTestsuite(String xmlReport) throws JAXBException, IOException
    public Testsuites parseTestsuites(String xmlReport) throws JAXBException, IOException
}
```

### DTO классы результатов

**TestExecutionResult** — агрегированный результат прогона:
```java
record TestExecutionResult(
    int total,      // Общее число тестов
    int failures,   // Число проваленных тестов
    int errors,     // Число ошибок выполнения
    boolean solved  // Флаг успешного решения
)
```

**TestCase** — детальная информация о каждом тесте:
```java
record TestCase(
    String id,
    String name,
    String status,
    String message,
    String errorType,
    String errorMessage,
    String errorDetails,
    long executionTimeMs,
    String stdout,
    String stderr
)
```

### JAXB модель JUnit

Классы пакета `org.supremecode.pluginsdk.junit` являются результатом code generation из XSD-схемы `jenkins-junit.xsd`. Схема определяет иерархию элементов:

- **testsuites** — корневой контейнер (опциональный)
- **testsuite** — набор тестов с атрибутами: name, tests, failures, errors, time, disabled, skipped, timestamp, hostname, id, package
- **testcase** — отдельный тест: name, assertions, time, classname, status
- **failure** — провал теста: type, message, содержимое (текст ошибки)
- **error** — ошибка выполнения: type, message, содержимое
- **properties** — контейнер свойств набора тестов
- **property** — пара name/value для метаданных
- **system-out** — стандартный вывод
- **system-err** — стандартный вывод ошибок

### Зависимости pom.xml

- `jackson-databind` 2.15.3 — JSON сериализация
- `jakarta.xml.bind-api` 4.0.1 + `jaxb-runtime` 4.0.2 — JAXB для XML parsing
- `javax.activation` 1.1.1 — активация JAXB
- `commons-io` 1.3.2 — утилиты для IO операций

---

## 2. common-plugins

### Назначение

Библиотека с эталонными реализациями интерфейса `LanguageTester` для поддерживаемых языков программирования. Содержит три плагина: Java, C++, JavaScript.

### Структура модуля

```
common-plugins/
├── src/main/java/org/supremecode/commonplugins/
│   ├── JavaLanguageTester.java    # Плагин для Java (JUnit 4)
│   ├── CppLanguageTester.java     # Плагин для C++ (GoogleTest)
│   └── JsLanguageTester.java      # Плагин для JavaScript (Jest)
└── pom.xml
```

### JavaLanguageTester

Предназначен для тестирования Java-решений с использованием Maven Surefire Plugin.

Конфигурация путей:
- **imageName**: `sc-java-test` — Docker-образ с настроенным Maven-проектом
- **testsPath**: `/src/test/java/JunitTest.java` — путь к файлу тестов
- **solutionPath**: `/src/main/java/org/example/Solution.java` — путь к решению
- **reportPath**: `/usr/app/target/surefire-reports/TEST-JunitTest.xml` — расположение JUnit XML

Логика verdict():
```java
TestExecutionResult verdict(String report, int exitCode) {
    // Парсит XML с помощью JunitParser
    // Извлекает total, failures, errors из атрибутов тестсьюиты
    // solved = failures == 0 && errors == 0 && exitCode == 0
}
```

### CppLanguageTester

Предназначен для тестирования C++-решений с использованием GoogleTest.

Конфигурация путей:
- **imageName**: `sc-cpp-test` — Docker-образ с CMake + GoogleTest
- **testsPath**: `/sc_test.cc` — путь к файлу с тестами
- **solutionPath**: `/solution.hpp` — заголовочный файл с решением
- **reportPath**: `/usr/app/build/junit.xml` — путь к отчёту в формате JUnit

Логика verdict() идентична JavaLanguageTester.

### JsLanguageTester

Предназначен для тестирования JavaScript-решений с использованием Jest.

Конфигурация путей:
- **imageName**: `sc-js-test` — Docker-образ с Node.js + Jest
- **testsPath**: `/test.js` — путь к тестовому файлу
- **solutionPath**: `/solution.js` — путь к файлу решения
- **reportPath**: `/usr/app/junit.xml` — отчёт Jest

Отличие в парсинге: использует `parseTestsuites()` (обёртка для множества наборов тестов), тогда как Java и C++ используют `parseTestsuite()` для одиночного набора.

### Механизм ServiceLoader

Класс `LanguagePluginService` в test-runner использует `java.util.ServiceLoader` для автоматического обнаружения реализаций `LanguageTester`:

```java
public class LanguagePluginService {
    Map<String, LanguageTester> map = new HashMap<>();

    LanguagePluginService() {
        ServiceLoader<LanguageTester> loader = ServiceLoader.load(LanguageTester.class);
        for (LanguageTester service : loader) {
            map.put(service.languageId(), service);
        }
    }
}
```

Для автоматического обнаружения необходимо создать файл:
`META-INF/services/org.supremecode.pluginsdk.LanguageTester`

---

## 3. test-runner

### Назначение

Микросервис, выполняющий тестирование пользовательских решений в изолированных Docker-контейнерах. Является потребителем Kafka для получения задач и публикует результаты обратно в Kafka. Взаимодействует с MinIO для получения тестов и загрузки результатов.

### Архитектура

```
test-runner/
├── src/main/java/org/supremecode/testrunner/
│   ├── TestRunnerApplication.java         # Точка входа, конфигурация Spring
│   ├── Listener.java                      # Kafka consumer
│   ├── Tester.java                        # Логика запуска контейнеров
│   ├── LanguagePluginService.java         # ServiceLoader integration
│   ├── LogCallback.java                   # Callback для сбора логов
│   ├── WaitCallback.java                  # Callback для ожидания контейнера
│   ├── SetTimeout.java                    # Future для timeout
│   ├── dto/TestResult.kt                  # DTO результата тестирования
│   └── configuration/
│       ├── SupremeCodeConfiguration.kt    # Свойства test-runner
│       ├── MinioConfig.java               # Bean MinIO client
│       └── MinioProperties.java           # Свойства подключения к MinIO
└── src/main/resources/
    ├── application.yaml                   # Конфигурация
    ├── xsd/jenkins-junit.xsd              # XSD схема для code generation
    └── *.xml                              # Примеры JUnit отчётов
```

### Технологический стек

- **Spring Boot 3.2.3** — фреймворк приложения
- **Spring Kafka** — интеграция с Apache Kafka
- **Docker Java 3.3.4** — API для управления Docker-контейнерами
- **MinIO Java SDK 8.5.7** — клиент объектного хранилища
- **Kotlin 1.9.24** — для конфигурационных классов и DTO

### Конфигурация Kafka

**Producer** (отправка результатов):
```yaml
bootstrap.servers: localhost:9092
key.serializer: StringSerializer
value.serializer: JsonSerializer (TestResultMessage)
topic: test-result-topic
```

**Consumer** (получение задач):
```yaml
bootstrap.servers: localhost:9092
key.deserializer: StringDeserializer
value.deserializer: JsonDeserializer (TestMessage.class)
group.id: test-group
topic: test-topic
```

### Конфигурация Docker

DockerClient создаётся с кастомным HTTP-клиентом (Apache 5):
```java
.withDockerHost("tcp://localhost:2375") // Windows
// или unix:///var/run/docker.sock для Linux
.maxConnections(100)
.connectionTimeout(Duration.ofSeconds(30))
.responseTimeout(Duration.ofSeconds(45))
```

### Listener (Kafka Consumer)

Основной обработчик входящих сообщений:

```java
@KafkaListener(topics = "test-topic", groupId = "test-group")
protected void listen(@Payload TestMessage testMessage, @Header(...) String messageId) {
    // 1. Получает тесты из MinIO (bucket: problems)
    // 2. Получает решение из MinIO (bucket: solutions)
    // 3. Находит LanguageTester по languageId
    // 4. Создаёт Tester и запускает test()
    // 5. Сохраняет logs.txt и report.txt обратно в MinIO
    // 6. Публикует TestResultMessage в resultTopic
}
```

### Tester (Container Orchestration)

Класс Tester управляет жизненным циклом Docker-контейнера:

**1. Создание архива (createArchive)**:
- Формирует TAR-файл, содержащий файл тестов и файл решения
- Использует `TarArchiveOutputStream` из Apache Commons Compress
- Пути файлов берутся из `LanguageTester.testsPath()` и `LanguageTester.solutionPath()`

**2. Создание контейнера (createContainer)**:
```java
HostConfig()
    .withMemory(400 MB)
    .withMemorySwap(1 GB)
    .withNanoCPUs(1_000_000_000L) // 1 ядро
    .withSecurityOpts(List.of("no-new-privileges"))
    .withUlimits(nofile: 468-512, nproc: 58-64)
    .withCapDrop(Capability.NET_ADMIN)
    .withNetworkMode("none") // Изоляция сети
```

**3. Процесс тестирования (test)**:
```java
CompletableFuture<TestResult> test(String tests, String code) {
    // 1. Создать контейнер
    // 2. Скопировать TAR-архив в /usr/app контейнера
    // 3. Запустить контейнер
    // 4. Подписаться на логи (LogCallback)
    // 5. Дождаться завершения или timeout (12 сек по умолчанию)
    // 6. Скопировать отчёт из контейнера
    // 7. Вызвать languageTester.verdict(report, exitCode)
    // 8. Убить и удалить контейнер
}
```

**Concurrency**: Используется `CompletableFuture.anyOf()` для race между:
- WaitCallback — результат контейнера
- SetTimeout — таймаут (по умолчанию 12 секунд)

### DTO TestResult

```kotlin
data class TestResult(
    val total: Int,         // Всего тестов
    val failures: Int,      // Провалы
    val errors: Int,        // Ошибки
    val solved: Boolean,    // Решение принято
    val statusCode: Int,    // Код завершения (-1 если timeout)
    val report: String,    // JUnit XML отчёт
    val logs: String,      // Логи контейнера
)
```

### XSD Code Generation

В `pom.xml` настроен плагин Apache CXF XJC для генерации JAXB классов из XSD:

```xml
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-xjc-plugin</artifactId>
    <version>4.0.0</version>
    <configuration>
        <sourceRoot>${basedir}/target/generated-sources/xsd</sourceRoot>
        <xsdDir>${basedir}/src/main/resources/xsd</xsdDir>
        <packagename>net.danil.generated.junit</packagename>
    </configuration>
</plugin>
```

Генерирует классы в `target/generated-sources/xsd/net/danil/generated/junit/`, однако на данный момент используются классы из `org.supremecode.pluginsdk.junit`.

### Конфигурация приложения (application.yaml)

```yaml
supreme-code:
  test-runner:
    container:
      ttk: 12000  # Time-to-kill в миллисекундах

minio:
  url: http://localhost:9000
  access-key: minio
  secret-key: minio123
```

### Взаимодействие с MinIO

- **Чтение**: `GetObjectArgs.builder().bucket("problems").object(...)` — получение тестов
- **Запись**: `PutObjectArgs.builder().bucket("solutions").object(...).stream(...)` — сохранение logs.txt и report.txt

---

## Схема взаимодействия модулей

```
┌───────────────────────────────────────────────────────────────────────┐
│                           test-runner                                 │
│  ┌─────────────┐    ┌──────────────────────────────────────────────┐  │
│  │  Listener   │    │               Tester                         │  │
│  │ (Kafka)     │───►│  createContainer()                           │  │
│  │             │    │  createArchive()                             │  │
│  │             │    │  copyArchiveToContainer()                    │  │
│  │             │    │  startContainer()                            │  │
│  │             │    │  waitContainer() ───► copyReport()           │  │
│  └─────────────┘    │       │                                      │  │
│                     │       ▼                                      │  │
│                     │  languageTester.verdict()                    │  │
│  ┌──────────────────┴──────────────────────────────────────────┐   │  │
│  │          LanguagePluginService (ServiceLoader)                  │  │
│  │  map: { "java" → JavaLanguageTester, "cpp" → CppLanguageTester }│  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                           plugin-sdk                                 │
│  ┌─────────────────┐    ┌─────────────────────────────────────────┐  │
│  │ LanguageTester  │◄───│ JunitParser                             │  │
│  │ (interface)     │    │ parseTestsuite(xml) → Testsuite         │  │
│  │                 │    │ parseTestsuites(xml) → Testsuites       │  │
│  └─────────────────┘    └─────────────────────────────────────────┘  │
│                                    │                                 │
│                                    ▼                                 │
│                           org.supremecode.pluginsdk.junit            │
│                           (JAXB model: Testsuites, Testsuite, etc.)  │
└──────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         common-plugins                                │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐    │
│  │JavaLanguageTester│  │CppLanguageTester  │  │JsLanguageTester  │    │
│  │ image: sc-java   │  │ image: sc-cpp     │  │ image: sc-js     │    │
│  │ report: surefire │  │ report: CMake     │  │ report: Jest    │    │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```

## Паттерны проектирования

1. **Plugin Pattern**: `LanguageTester` — точка расширения для новых языков
2. **ServiceLoader Pattern**: Динамическая загрузка плагинов без конфигурации
3. **Strategy Pattern**: Выбор `LanguageTester` по `languageId`
4. **Template Method**: `Tester.test()` определяет общую последовательность, детали в `LanguageTester`
5. **Callback/Observer**: `LogCallback`, `WaitCallback` для асинхронных событий Docker
6. **DTO/Record**: Неизменяемые объекты данных (`TestExecutionResult`, `TestResult`, `TestCase`)

## Расширяемость

Для добавления нового языка необходимо:
1. Создать реализацию `LanguageTester` в отдельном модуле
2. Добавить JAR в classpath test-runner
3. Создать файл `META-INF/services/org.supremecode.pluginsdk.LanguageTester` с полным именем класса
4. Подготовить Docker-образ с тестовым фреймворком для языка