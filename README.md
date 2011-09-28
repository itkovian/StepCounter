StepCounter Android app
-----------------------

The StepCounter aims to detect footsteps by using the accelerometer that's
built into most modern Android hardware platforms.

The purpose of this tool is to serve as a project for the first bachelor
engineering in Computer Science at Ghent University.

Design
======

The main Activity does little else than show the user the number of steps and a
button to quit the app. All actual work is done in a backend Service. This
service acts as a SensorEventListener that registers itself to receive the
events from the (changing) accelerometer. It writes the raw data to a file,
name given by the Activity. It also logs the data, so adb logcat should provide
an realtime output of the measured values.

TODO
====

- Enhance the UI to include a field where the number of detected steps can be
  shown.
- Add a BroadcastReceiver (?) to increase the steps value shown in the
  Activity. The Service should broadcast the increase when a step is detected.
- Store the filename for the data logging in the strings.xml file and use that
  resource rather than hard-coding it in the Activity code itself.
- Write the actual detection code
- Play around with sampling rates. How high can we get this for the test
  platforms we have.
