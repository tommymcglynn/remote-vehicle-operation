FROM balenalib/rpi-raspbian:latest

# Set the working directory to /app
WORKDIR /app

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get -y install apt-utils && \
    apt-get -y install \
    apt-transport-https \
    ca-certificates \
    git \
    libgtk2.0-dev \
    libavcodec-dev \
    libavformat-dev \
    libswscale-dev \
    libtbb2 \
    libtbb-dev \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    libjasper-dev \
    libdc1394-22-dev \
    protobuf-compiler \
    unzip \
    wget \
    wiringpi

RUN wget https://www.dropbox.com/s/qwa3kez2m5qn1xo/jdk-linux-arm32-1.8.0_211.zip && unzip jdk-linux-arm32-1.8.0_211.zip && rm jdk-linux-arm32-1.8.0_211.zip
RUN wget https://www.dropbox.com/s/avsh6bq08nvr9sh/opencv-linux-arm32-3.4.6.zip && unzip opencv-linux-arm32-3.4.6.zip && rm opencv-linux-arm32-3.4.6.zip
RUN curl -sSL https://pi4j.com/install | sudo bash
RUN usermod -a -G video root

ENV JAVA_HOME /app/jdk-linux-arm32-1.8.0_211
ENV PROTOC_PATH /usr/bin/protoc
ENV OPENCV_DIR /app/opencv-linux-arm32-3.4.6

# Make port 8080 available to the world outside this container
EXPOSE 8080

ENV APP_HOME /app
COPY gradle $APP_HOME/gradle/
COPY build.gradle settings.gradle gradlew $APP_HOME/
COPY data/build.gradle $APP_HOME/data/
COPY shared/build.gradle $APP_HOME/shared/
COPY vehicle-control/build.gradle $APP_HOME/vehicle-control/
COPY vehicle-node/build.gradle $APP_HOME/vehicle-node/
RUN ./gradlew vehicle-node:build -Pprotoc.path=$PROTOC_PATH -Popencv.dir=$OPENCV_DIR || return 0

# Copy the current directory contents into the container at /app
COPY . $APP_HOME

WORKDIR $APP_HOME
RUN ./gradlew vehicle-node:build -Pprotoc.path=$PROTOC_PATH -Popencv.dir=$OPENCV_DIR

CMD $JAVA_HOME/bin/java -Djava.library.path=$OPENCV_DIR/lib -Dcar.node.class=com.mcglynn.rvo.vehicle.toy.FourWheelToyCarNode -Dcamera.id=-1 -jar vehicle-node/build/libs/vehicle-node-1.0-SNAPSHOT.jar
