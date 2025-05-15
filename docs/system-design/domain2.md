# User

- email: string
- username: string
- password: string
- image: string
- roles: []
    - user
    - admin
- permissions: string[]

Функционал над текущим пользователем или над любым, если текущий - admin:

- Регистрация
- Авторизация
- Удаление пользователя
- Смена пароля
- Смена изображения

# Problem

Задача на платформе. 

- author: User
- name: string
- description: string
- tags: Tag[]
- revision: number
- isPublished: boolean
- languages: []
    - completeSolution: string
      Файл с кодом полного решения
    - initialSolution: string
      Файл с кодом шаблона для решения
    - preloaded: string
      Дополнительный необязательный файл с бойлерплейт кодом
    - tests: string
      Файл с кодом тестов

Функционал пользователя:

- Создание, редактирование, удаление, сохранение, публикация задачи. (При наличии права)
- Получение списка задач.
- Получение подробной информации о задаче.

# Tag

- name: string

Функционал:

- Админ: создание, редактирование, удаление
- Пользователь: получение списка тегов

# Language

- name: string
- image: Docker Image
  Образ с рантаймом языка и фреймворком тестирования.
  Фреймворк настроен на вывод результатов в формате junit.xml.
- podManifest: string
  Kubernetes manifest для запуска ПОДа. Содержит название образа, начальную директорию, команду запуска и прочее.
- testRunner: Java класс для запуска автоматизированного тестирования.
- taskRunner: Java класс для запуска кода в Code Playground.

Функционал:

- Получение списка языков
- Интеграция новых языков через плагинизацию

# Solution

Решение отправляемое пользователем на проверку.

- user: User
- language: Language
- code: string

Функционал:

- Отправка решения на проверку
- Получение списка решений и результатов проверки (своих)

## SolutionResult

Результат проверки решения отправленного пользователем.

- solution: Solution
- exitCode: number
- stderr: string
- stdout: string
- timedOut: boolean
- time: number
- completed: boolean
- tests: { name: string, passed: boolean }[] - value object

Функционал:

- Создать результат проверки решения (действие системы)

# CollabRoom

Комната для совместного редактирования кода решения.

- problem: Problem
- host: User
- link: string
- members: User[]
- code: string

Функционал:

- Создание комнаты
- Подключение к комнате по ссылке
