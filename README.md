# Remote Vehicle Operation
Remotely operate a vehicle. Currently designed to operate a Raspberry Pi equiped vehicle.

## Vehicle Node
Accepts car controller commands, translates commands into vehicle control and responds with vehicle state information. Also sends video data to the target specified by controller command.

### Deploy Vehicle Node
Use a gradle task to compile and deploy the vehicle application. The vehicle server will be restarted.

    ./gradlew vehicle-node:deploy -PvehicleHost=192.168.1.85

### Run Configuration
These are properties which can be used to configure the vehicle node.

JVM
* java.library.path - Must be set to the location of compiled native OpenCV libraries. Example: /Users/tommy/opencv/build/lib

System
* car.port (default: 8080) - This is the port that the vehicle server will run on.
* car.node.class (default: com.mcglynn.rvo.vehicle.toy.FourWheelToyCarNode) - This is the implementation class of CarNode which will be used to handle car commands and operate a vehicle.

### Build Configuration
Configure these properties appropriately in your Gradle directory "~/.gradle"

    // This points to compiled OpenCV bindings for Java
    // See https://github.com/opencv/opencv
    opencvDir=/Users/tommy/opencv/build

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
