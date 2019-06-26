# Remote Vehicle Operation
Remotely operate a vehicle. Currently designed to operate a Raspberry Pi equiped vehicle.

![Remote Vehicle Operation - Title Image](https://www.dropbox.com/s/6tshswvccgkknis/rvo_title.png)

[Video Demonstration](https://www.youtube.com/watch?v=Uds8QS7zHmM)

## Vehicle Node
Accepts car controller commands, translates commands into vehicle control and responds with vehicle state information. Also sends video data to the target specified by controller command.

### Build and Deployment
The vehicle-node application is built as a Docker image on the vehicle system itself. It can then be run as a Docker container on the vehicle.

1. Make code changes to vehicle logic
2. SSH to vehicle

        ssh vehicle@192.168.1.12

3. Pull latest code

	    // Clone first if you haven't done so already
        git clone && cd remote-vehicle-operation
        // Otherwise, pull latest code
        cd remote-vehicle-operation && git pull

4. Build Docker image
	
	    docker build -t rvo-base -f ./docker/base/Dockerfile .

5. Run as Docker container

	    docker run -i -t --privileged -p 8080:8080 rvo-base


### Run Configuration
These are properties which can be used to configure the vehicle node.

JVM
* java.library.path - Must be set to the location of compiled native OpenCV libraries. Example: /Users/tommy/opencv/build/lib

System
* protoc.path (default: /usr/local/bin/protoc) - Points to protoc executable for compiling Protocol Buffers
* opencv.dir (default: /home/vehicle/opencv) - Points to OpenCV native library directory
* car.port (default: 8080) - This is the port that the vehicle server will run on.
* car.node.class (default: com.mcglynn.rvo.vehicle.toy.FourWheelToyCarNode) - This is the implementation class of CarNode which will be used to handle car commands and operate a vehicle.
* camera.id (default: 0) - Camera ID to use for video capture

        ./gradlew vehicle-node:run -Pprotoc.path=/home/vehicle/src/protoc -Popencv.dir=/home/vehicle/opencv-3.4.6/build -Pcar.node.class=com.mcglynn.rvo.vehicle.toy.FourWheelToyCarNode -Pcamera.id=-1


### Vehicle Node Architecture

* CarNodeApplication is the main entry point
* CarNodeServerHandler handles socket channel events and converts socket data into CarControllerCommand
* CarNode interface handles CarControllerCommand
* FourWheelToyCarNode is an implementation of CarNode which controls a Raspberry Pi based toy car

### Vehicle Logs
Vehicle logs are at $home/logs/vehicle-node.log

### Raspberry Pi Setup
These are initial setup steps.

* Create user on Pi machine for running the vehicle-node app. ie: "vehicle"
* Add user to the gpio Linux group for access to GPIO pins

      sudo usermod -a -G gpio vehicle
    
* Create a new RSA key pair on the development machine so you can connect and deploy to the Pi. ie: "vehicle_id_rsa"
* Copy the contents of the public portion of the key "vehicle_id_rsa.pub" and past them into the authorized_keys file on the Pi machine "/home/vehicle/.ssh/authorized_keys"
* Read and update "vehicle-node.sh" which is responsible for starting and stopping the vehicle-node app on the Pi machine. This gets copied to the Pi through a gradle task.
* Read and update "build.gradle" to ensure all parameters are correct.
* Install Pi4J. Allows control of GPIO pins from Java: https://pi4j.com
* Compile and install OpenCV. This has been done in "/home/vehicle/opencv": https://docs.opencv.org/master/d9/d52/tutorial_java_dev_intro.html
* Enable Pi Camera via raspi-config
* Enable Pi Camera drivers for OpenCV

        sudo modprobe bcm2835-v4l2

* Add vehicle user to video group, for access to camera

        sudo usermod -a -G video vehicle

* Install Docker

        # Download and run Docker install script
        sudo curl -sSL https://get.docker.com | sh
        # Enable auto-start
        sudo systemctl enable docker
        # Start docker
        sudo systemctl start docker
        # Allow vehicle user to use Docker client
        sudo usermod -aG docker vehicle
    

### Motor Control
6 GPIO pins are used to control motor function.

* Drive 1: 11 enable, 13 reverse, 15 speed (PWM)
* Drive 2: 29 enable, 31 reverse, 33 speed (PWM)

### Configuration
These are system properties which can be used to configure the FourWheelToyCarNode

* fwtc.steer.drive.reduction (default: 0.5) - The amount of drive reduction multiplier for steer
* fwtc.max.drive.delay.millis (default: 100) - The millisecond delay to apply max motor drive


## Vehicle Control
Keeps trying to connect to a vehicle node server. Once successful, will start generating and sending controller commands. Also receives vehicle state from vehicle node.

### Vehicle Control Architecture

* CarControllerApplication is the main entry point
* CarControllerClientHandler handles socket channel events
* CarClientConfig is a configuration for CarControllerClientHandler
* CarController is an interface which exposes car commands via CarControllerCommand and handles vehicle state via CarData

### Configuration
These are system properties which can be used to configure the vehicle controller.

* car.host (default: localhost) - The host that the controller will use to connect to the vehicle node
* car.port (default: 8080) - The port that the controller will use to connect to the vehicle node
* controller.command.delay (default: 30) - Millisecond delay between sending car commands
* video.receive.port (default: 8090) - This is the port that the vehicle controller will receive video on
