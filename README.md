# Demo PSCR'19 Java (PC) vesion
The Link (D2D) and Network (Naming) layers are implemented as a library. We can easily use these items for other applications (including inserting them into Web Servers.


## Prerequests
```
sudo apt install maven
```

## Build library first
```
cd Demo-PSCR19-Java
mvn clean install
cd ..
```

## Build runner
```
cd Demo-PSCR19-Java-Runner
mvn clean package
cd ..
```

## To run the application
```
java -jar Demo-PSCR19-Java-Runner/target/Demo-PSCR19-Java-Runner-1.0-jar-with-dependencies.jar
```
