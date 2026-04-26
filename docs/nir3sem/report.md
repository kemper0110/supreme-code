# Делаю

- Отчет
- Блочёк 2 главы ВКР то же самое
- Презентация
- Еще раз презу приложить
- Как-то указать статью... сканом

Кратко
- Отчет
- Презентация
- Приложить статью


# 1 Постановка задачи

Задача 1 - проектирование хранения файлов в MinIO
Задача 2 - проектирование плагинной архитектуры

# 2 Проектирование плагинной архитектуры

**Backend**

Для java решено использовать ServiceLoader.

В SDK есть:
Интерфейсы сервисов LanguageInfo, LanguageWeb, LanguageTester.
LogCallback, Tester.
Result, Junit JAXB Parser.
Тесты поиска плагина.

В плагине будет:
Реализации интерфейсов сервисов. 
Результат сборки fat-jar с shading и всеми зависимостями.

Абстрактный класс Tester уже содержит базовый функционал для популярных операций:
- создать tar архив с файлами
- создать контейнер с ограничениями
- скачивание tar архива с результатами тестирования из контейнера
- основной путь по запуску тестирования
  - создать контейнер
  - скопировать исходники в контейнер
  - запустить контейнер
  - слежение за логами
  - удаление контейнера

Разработчику плагину нужно реализовать эти интерфейсы.
Собрать проект в jar-ку.

Платформа подгружает все jar-ки из папки plugins или любой другой сконфигурированной папки,
используя изолированный URLClassLoader.

```java
public interface LanguageInfo {
    String getId();
    String getDisplayName();
    String getMonacoLanguageId();
    String getMonacoLanguageJsModule();
}
public interface LanguageTester {
    TestExecutionResult test(
            DockerClient dockerClient, 
            Problem problem, 
            Solution solution
    );
}
public interface LanguageWeb {
    void handleRequest(
            HttpServletRequest request,
            HttpServletResponse response
    );
}
```

**Docker**

Просто чел использует базовые образы, которые ему нравятся.
Добавляет туда библиотеки, заранее подгружает их.
Настраивает тестовый фреймворк, выбирает пути для исходников и результатов тестирования. 
Создает базовый проект и копирует его в образ. 
Формирует команду для компиляции и запуска будущих тестов.

**Frontend**

Плагинизация языков касается не только бэкенда. На фронтенде есть редактор кода и селекторы языков, 
поэтому фронтенд тоже должен плагинизироваться.
О существовании плагинов фронтенд узнает запросом к веб-серверу.
В ответ получает список всех доступных языков и их настройки.
Одним из полей настроек является ссылка на js-модуль поддержки языка.
Для загрузки этого модуля решено использовать динамический импорт ES модулей.

```js
const module = await import(importPath)
```

При импорте модуль должен зарегистрировать свой язык в monaco-editor.
Модуль может располагаться в любой папке доступной из браузера. (не знаю как объяснить, nginx должен раздавать эту папку)
В том числе можно использовать эндпоинт из плагина веб-сервера. 


# 3 Проектирование хранения файлов в MinIO

Хранилище MinIO
/users/{userId}/solutions/{solutionId}
/solution.txt
/logs.txt
/result.json
Файлы рассматриваются в разрезе конкретного пользователя. Это может быть его решение.
Выбрал solution.TXT, потому что это по сути текст, а не код на конкретном языке. Выглядит достаточно удачно.


Как хранить задачки
/system/problems/{problemId}
/test.txt
/pre.txt
/solution-template.txt
/solution.txt

# TODOOOOO

- [x] Содержание 
- [x] Список литературы 
- [x] Количество страниц

MinIO
Плагинная архитектура
java service loader
monaco editor
sdk
URLClassLoader
ES modules
API Amazon S3


MinIO — объектное хранилище с открытым исходным кодом [Электронный ресурс], URL: https://min.io/ (дата обращения: 19.01.2026).

Плагинная архитектура — статья в Википедии [Электронный ресурс], URL: https://ru.wikipedia.org/wiki/Плагинная_архитектура (дата обращения: 19.01.2026).

Java Service Loader — документация Oracle [Электронный ресурс], URL: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html (дата обращения: 19.01.2026).

Monaco Editor — редактор кода от Microsoft [Электронный ресурс], URL: https://microsoft.github.io/monaco-editor/ (дата обращения: 19.01.2026).

SDK — статья в Википедии [Электронный ресурс], URL: https://ru.wikipedia.org/wiki/SDK (дата обращения: 19.01.2026).

URLClassLoader — документация Oracle [Электронный ресурс], URL: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URLClassLoader.html (дата обращения: 19.01.2026).

ES modules — документация MDN Web Docs [Электронный ресурс], URL: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Modules (дата обращения: 19.01.2026).

API Amazon S3 — официальная документация AWS [Электронный ресурс], URL: https://docs.aws.amazon.com/AmazonS3/latest/API/Welcome.html (дата обращения: 19.01.2026).