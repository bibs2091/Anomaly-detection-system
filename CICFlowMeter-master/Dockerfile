FROM gradle:4.2.1-jdk8-alpine

ENV APP_HOME=/CICFlowMeter/
WORKDIR $APP_HOME

COPY  ./ /CICFlowMeter/

USER root 
EXPOSE 25333

RUN chown -R gradle /CICFlowMeter              
RUN ./gradlew build --stacktrace

CMD ["./gradlew","execute"]