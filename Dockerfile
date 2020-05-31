# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="bushanphani9@gmail.com"

# Declaring arguments for Jar file
ARG JAR_FILE=target/*.jar

# Copying jar file
COPY ${JAR_FILE} app.jar

# Creating ENTRYPOINT
ENTRYPOINT ["java","-jar","/app.jar"]