FROM node:21-alpine
WORKDIR /usr/app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN npm update -g npm
RUN chown -R appuser:appgroup /usr/app

USER appuser
COPY package.json .
RUN npm i
COPY jest.config.js .
