## PLAN

- как хранить задачки - главный контент платформы.
    - [ ] в бд сохранять описание, шаблон кода и код файла с тестами
    - [x] контентный репозиторий с метаданными в json и файловой структурой для проекта
- какова глубина возможностей создания задачи?
    - структура проекта
        - [ ] полная копия структуры проекта - со сборкой образа
        - [x] полная копия структуры проекта - на существующем образе
        - [ ] частичное копирование - будет копироваться только структура из ./src/main и ./src/test
        - [ ] структура строго предопределена - Main.java и Test.java
    - форматированное описание
        - язык
            - [x] MARKDOWN
            - [ ] HTML
            - [ ] WYSIWYG редактор HTML
        - картинки
            - [x] отсутствуют
            - [ ] вкладываются из директории
- продумать логику работы и отображения самбитов
    - список прошлых сабмитов
        - [ ] хранить только последний верный
        - [x] хранить все
    - [x] отображаю, что задача уже была решена


-- Мигрировать репозитории в отдельную учетку

## Queries

### Problem searching
- Найти все теги
- Найти все задачи по фильтрам
  - Название
  - Сложность
  - Язык
  - Множество тегов
  
- Найти все подборки задач
  - Пример: Изучите C++ за 20 задач zero-to-hero гайд
    - Название
    - Картинка
    - Описание
    - Список задач

- Достижения пользователя
- ??
    
### Инфографика юзерская
Выбор по видам графиков RadarChart, BarChart, PieChart, DonutChart.
Куча чекбоксов, ползунков, селектов, чтобы настроить вывод данных. 
Для начала только один

С выбором графика: 
- Решенные и начатые задачи
  - `select s.problem_slug, max(sr.solved::int)
    from solution s inner join solution_result sr on s.id = sr.solution_id
    where user_id = ?
    group by s.problem_slug`;
- Любимый язык: 3 столбца по количеству
  - `select language, count(*)
    from (select user_id, problem_slug, max(sr.solved::int) as solved, language
    from solution s
    inner join solution_result sr on s.id = sr.solution_id
    group by user_id, problem_slug, language) as problem_result
    group by language`
- Сложность задач: 3 категории по количеству
  - `select problem_slug, count(*)
    from (select user_id, problem_slug, max(sr.solved::int) as solved, language
    from solution s
    inner join solution_result sr on s.id = sr.solution_id
    where user_id = ?
    group by user_id, problem_slug, language) as problem_result
    group by problem_slug;`
  - или
  - `select problem_slug, count(*)
    from (select distinct user_id, problem_slug, language
    from solution s
    inner join solution_result sr on s.id = sr.solution_id
    group by user_id, problem_slug, language) as problem_result
    group by problem_slug;`
  - Далее из файла читаем сложность по каждой задаче и суммируем группируя по сложности

AreaChart: 
- Количество решенных задач в каждом месяце 
  - добавить поле времени сабмита
  - `select s.problem_slug
    from solution s
    inner join solution_result sr on s.id = sr.solution_id
    where user_id = 2 and sr.solved = true
    group by s.problem_slug`
- ? Среднее количество попыток до первого верного решения

### Инфографика общая
Таблицы
- топ N задач по количеству решивших
  - `select problem_slug, count(*)
    from (select user_id, problem_slug, language
    from solution s
    inner join solution_result sr on s.id = sr.solution_id
    where sr.solved = true
    group by user_id, problem_slug, language) as problem_result
    group by problem_slug;`
- топ N задач по количеству приступивших
  - `select problem_slug, count(*)
    from (select distinct user_id, problem_slug, language
    from solution s
    inner join solution_result sr on s.id = sr.solution_id) as problem_result
    group by problem_slug;`
- топ N задач по количеству приступивших, но не решивших
  - `select problem_slug, count(*)
    from (select user_id, problem_slug, language, max(sr.solved::int) as solved
    from solution s
    inner join solution_result sr on s.id = sr.solution_id
    group by user_id, problem_slug, language having max(sr.solved::int) = 0) as problem_result
    group by problem_slug;`

С выбором графика как у юзера
- Сложность задач: 3 категории по количеству, но уже по всем пользователям
- Любимый язык: 3 столбца по количеству, но уже по всем пользователям
