# Remote Vehicle Operation
Remotely operate a vehicle. Currently designed to operate a Raspberry Pi equiped vehicle.

## Raspberry Pi
Required setup.

* Create user on Pi machine for running the vehicle-node app. ie: "vehicle"
* Add user to the gpio Linux group for access to GPIO pins

      sudo usermod -a -G gpio vehicle
    
* Create a new RSA key pair on the development machine so you can connect and deploy to the Pi. ie: "vehicle_id_rsa"
* Copy the contents of the public portion of the key "vehicle_id_rsa.pub" and past them into the authorized_keys file on the Pi machine "/home/vehicle/.ssh/authorized_keys"
* Read and update "vehicle-node.sh" which is responsible for starting and stopping the vehicle-node app on the Pi machine. This gets copied to the Pi through a gradle task.
* Read and update "build.gradle" to ensure all parameters are correct. In particular, check the "vehicleHost" to ensure it is the correct host where you can reach the Pi machine.
* Install Pi4J. Allows control of GPIO pins from Java: https://pi4j.com

## Basic Architecture
The vehicle controller app starts and keeps trying to connect to the vehicle node. Once the connection is successful, the controller will start sending car commands, based on user input.

## Motor Control
6 GPIO pins are used to control motor function.

* Drive 1: 11 enable, 13 reverse, 15 speed (PWM)
* Drive 2: 29 enable, 31 reverse, 33 speed (PWM)