@echo off
REM Navigate to the 'server' directory
cd /d ".\server"

REM Open a new terminal window and run the Maven command
start "Server 0" cmd /k mvn exec:java -D"exec.args"="8080 0"
start "Server 1" cmd /k mvn exec:java -D"exec.args"="8080 1"
start "Server 2" cmd /k mvn exec:java -D"exec.args"="8080 2"
start "Server 3" cmd /k mvn exec:java -D"exec.args"="8080 3"
start "Server 4" cmd /k mvn exec:java -D"exec.args"="8080 4"


cd /d ".\.."
cd /d ".\client"\

start "Client" cmd /k mvn exec:java

cd /d ".\.."
cd /d ".\consoleclient"\

start "Control Console" cmd /k mvn exec:java


REM Close the current terminal window
exit
