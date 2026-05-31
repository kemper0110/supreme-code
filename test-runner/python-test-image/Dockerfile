FROM node:18-alpine

RUN apk add python3 py3-pip gfortran musl-dev gcc
RUN apk add py3-pandas py3-numpy
RUN apk add py3-pytest

WORKDIR /usr/app

COPY solution.py .
COPY test_solution.py .

ENTRYPOINT ["pytest"]
CMD ["--junitxml=report.xml"]