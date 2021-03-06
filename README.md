# Corridor Navigation [![Build Status](https://travis-ci.org/AMOS-Project-TU-Berlin-Team-2/Corridor-Navigation-Android.svg?branch=master)](https://travis-ci.org/AMOS-Project-TU-Berlin-Team-2/Corridor-Navigation-Android)

A car navigation system built by students of the Technische Universität Berlin. The goal of this project is to do real time alternative route calculations. This makes it possible to change your route immediately if something on your current route occurs.

<img  src="https://i.imgur.com/rUeyFDg.jpg" width="300" />


## Installation

New releases are built weekly on Thursday afternoon. Get the latest release as an APK from the [releases section](https://github.com/AMOS-Project-TU-Berlin-Team-2/Corridor-Navigation-Android/releases) in this repository.

## Setup development environment
```
git clone git@github.com:AMOS-Project-TU-Berlin-Team-2/Corridor-Navigation-Android.git
```
And import project into Android Studio. All necessary libraries and modules will be installed automatically.

## Testing

Every commit is automatically linted and tested by Tavis-CI, see the build status [here](https://travis-ci.org/AMOS-Project-TU-Berlin-Team-2/Corridor-Navigation-Android).

You can test and lint it locally using
```
gradlew test
```

## Building
```
gradlew assembleDebug
```
For a debugging apk

We need still have to setup a keystore in order to build production releases.
