FROM amazoncorretto:11

RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=0.55.1'

ADD target/bot-service-0.0.1-SNAPSHOT.jar /

ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar bot-service-0.0.1-SNAPSHOT.jar
