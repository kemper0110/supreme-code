FROM node:21-alpine as frontend-build
ENV YARN_VERSION=4.1.0
RUN apk update && apk upgrade && apk add --no-cache libc6-compat && apk add dumb-init
RUN corepack enable && corepack prepare yarn@${YARN_VERSION}
WORKDIR /usr/app
COPY . .
RUN yarn
RUN yarn build

FROM nginx:stable-alpine3.17-slim
RUN apk add curl
COPY --from=frontend-build /usr/app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
