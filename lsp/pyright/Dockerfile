FROM node:18-alpine

RUN apk add python3 py3-pip gfortran musl-dev gcc
RUN apk add py3-pandas py3-numpy
RUN npm install -g pyright

WORKDIR /workspace

COPY main.py .

# Запускаем LSP сервер в режиме stdio
ENTRYPOINT ["pyright-langserver", "--stdio"]