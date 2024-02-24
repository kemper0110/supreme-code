FROM node:21-alpine
WORKDIR /usr/app
RUN npm update -g npm
COPY . .
RUN npm i
CMD ["npm", "run", "test"]
# then
# docker cp /silly_hofstadter:/usr/app/junit.xml junit.xml