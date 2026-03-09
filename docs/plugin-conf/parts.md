Итоги.
Расширяемость ПО без поддержки разработчика все еще остается актуальной проблемой.
В презентации я показал, как эту задачу для своей платформы решил я. 


Как рассказать без спойлеров возможность кастомного эндпоинта.
Подгрузки инфы с фронта.
Точки кастомизации на фронте.


```typescript
export declare module 'language-plugin' {
    export function activate(context: PluginContext): void;

    export function deactivate(): void;
}
```