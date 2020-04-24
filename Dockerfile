FROM openjdk:11.0-slim

WORKDIR /server

COPY ./resource-data.xlsx resource-data.xlsx
COPY ./build/libs/BC-CPC-COVID-19-ws.jar web-server.jar
CMD java -jar web-server.jar
