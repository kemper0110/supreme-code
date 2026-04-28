FROM node:24
LABEL authors="Danil"

WORKDIR /user/app

COPY package.json package-lock.json ./
RUN npm ci

COPY tsconfig.json ./
COPY src ./src
COPY templates ./templates

CMD ["npm", "run", "prod:debug"]