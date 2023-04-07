FROM openjdk:21-jdk
MAINTAINER kcybulski.me
VOLUME /tmp
EXPOSE 8080 5000
ADD build/install/cloud-store-service /build
ENTRYPOINT ["./build/bin/cloud-store-service"]