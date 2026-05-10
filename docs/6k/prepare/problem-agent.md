
Используем готовый кодинговый агент типа
codex, opencode, claudecode, qwencode, continue cli, gigacode cli


- Не нужна сложная авторизация агента. 
- Не нужно перепродавать токены. 
- Чат
- Сессии
- Цикл разработки
- Работа с файлами
- Провайдеры, коннекторы, модели
- mcp
- skills
- RAG
- ...всё, что в будущем появится. это задел на будущее.

# Фронт

Кнопка для экспорта на диск.
Кнопка импорта с диска. 

Тулзы

- exportThisProblem
- importThisProblem
- validate
* listProblems
* exportProblem

Добавить описание для каждого языка? 

# Local MCP server

прокси по работе с фронтом.
сам не держит тулзы наверное.

# Agent Skill

https://habr.com/ru/companies/bitrix/articles/980654/

SKILL.md

---
name: supreme-code-problem
description: Используй этот навык при работе с задачами на платформе SupremeCode.
---

references/info.schema.json

```json
{
  "name": "Two Sum",
  "tags": [
    "math",
    "hashmap"
  ],
  "else": "..."
}
```