## Для запуска требуется собрать образы тестовых контейнеров

```powershell
docker build -t sc-js-test-image .\test-runner\js-test-image\
docker build -t sc-cpp-test-image .\test-runner\cpp-test-image\
docker build -t sc-java-test-image .\test-runner\java-test-image\
```