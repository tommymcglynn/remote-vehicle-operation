FROM balenalib/rpi-raspbian:latest

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Make port 8080 available to the world outside this container
EXPOSE 8080

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get -y install apt-utils && \
    apt-get -y install wget unzip && \
    wget https://www.dropbox.com/s/qwa3kez2m5qn1xo/jdk-linux-arm32-1.8.0_211.zip && unzip jdk-linux-arm32-1.8.0_211.zip && rm jdk-linux-arm32-1.8.0_211.zip && \
    wget https://www.dropbox.com/s/qmfqyawcjv6azzi/opencv-linux-arm7-4.0.1.zip && unzip opencv-linux-arm7-4.0.1.zip && rm opencv-linux-arm7-4.0.1.zip && \
    wget https://www.dropbox.com/s/bchii2x1k9wlqh6/protobuf-linux-arm7-3.7.0.zip && unzip protobuf-linux-arm7-3.7.0.zip && rm protobuf-linux-arm7-3.7.0.zip && \
    apt-get -y install wiringpi && \
    curl -sSL https://pi4j.com/install | sudo bash

ENV JAVA_HOME /app/jdk-linux-arm32-1.8.0_211

RUN ./gradlew

CMD ["./gradlew vehicle-node:run -PprotocPath=/app/protobuf-linux-arm7-3.7.0/protoc -PopencvDir=/app/opencv-linux-arm7-4.0.1/build"]