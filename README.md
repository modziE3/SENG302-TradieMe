# SENG302 Team 1000 project
A website using ```gradle```, ```Spring Boot```, ```Thymeleaf```, and ```GitLab CI```.

This is a website that allows renovators to manage their own home renovations.
Records can be made that contain all details about their ongoing renovations, including the rooms being renovated and jobs that need to be done.
These records can be shared so renovators can share their progress on their renovations.
Renovators can also post renovation jobs they are wanting help with, allowing tradies to find work.

## Using the application
The deployed website can be found [here](https://csse-seng302-team1000.canterbury.ac.nz/prod/)

## How to run locally
### 1 - Running the project
From the root directory ...

On Linux:
```
./gradlew bootRun
```

On Windows:
```
gradlew bootRun
```

By default, the application will run on local port 8080 [http://localhost:8080](http://localhost:8080)

### 2 - Running unit, integration and acceptance tests
On Linux:
```
./gradlew test
```
```
./gradlew integration
```
```
./gradlew cucumber
```

On Windows:
```
gradlew test
```
```
gradlew integration
```
```
gradlew cucumber
```

## Licence
Our full licence can be found at LICENCE.md

## Contributors

- SENG302 teaching team
- Alex Cheals
- Calan Meechang
- Dury Kim
- Fergus Ord
- Luke Burton
- Morgan Lee