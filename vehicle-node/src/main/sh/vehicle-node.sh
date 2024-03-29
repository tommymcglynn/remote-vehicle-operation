#!/bin/sh
SERVICE_NAME=vehicle-node
JAR_DIR=~/vehicle-node
PATH_TO_JAR=$JAR_DIR/vehicle-node-1.0-SNAPSHOT.jar
JVM_ARGS=-Djava.library.path=/home/vehicle/opencv-3.4.6/build/lib
PID_PATH_NAME=/tmp/vehicle-node-pid
OUTPUT=/dev/null
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java $JVM_ARGS -jar $PATH_TO_JAR /tmp 2>> $OUTPUT >> $OUTPUT &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java $JVM_ARGS -jar $PATH_TO_JAR /tmp 2>> $OUTPUT >> $OUTPUT &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac