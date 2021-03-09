##### Dependencies stage #####
FROM maven:3.6.3-amazoncorretto-11 AS dependencies
WORKDIR /usr/app

# Resolve dependencies and cache them
COPY pom.xml .
RUN mvn dependency:go-offline -s /usr/share/maven/ref/settings-docker.xml


##### Build stage #####
FROM dependencies AS build
COPY lombok.config .
COPY src/main src/main
RUN mvn clean package -s /usr/share/maven/ref/settings-docker.xml


##### Test stage #####
FROM build AS test
COPY src/test src/test
RUN mvn test -s /usr/share/maven/ref/settings-docker.xml


##### Assemble stage #####
FROM amazoncorretto:11 AS assemble

# Fetch the datadog agent
RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'
COPY --from=build /usr/app/target/bot-service-0.0.1-SNAPSHOT.jar .
ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar bot-service-0.0.1-SNAPSHOT.jar
