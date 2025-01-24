# Actify - The Activity Recognition App
This project applies machine learning techniques on data collected
from a Respeck inertial measurement unit (IMU) to classify a range of 
activities and social signals, displaying the results in real-time in 
an Android app. The Respeck device is worn on the lower left
ribcage, and collects linear acceleration data along three axes.
Two Convolutional Neural Networks (CNNs) were trained on this collected data,
one for classifying activities and one for classifying social signals.
An overview of the application structure is shown below. Data is streamed from the 
Respeck device at 25 Hz, filling two buffers. Once the buffers are full,
the two CNNs both make independent classifications, and then the oldest
50 data points are removed from the buffers. The classification results
are displayed in the app and stored in a database so the user can reflect
on their daily activity and social signal patterns.
![App Architecture](https://github.com/user-attachments/assets/a66fbc8d-e3bd-43e1-abe3-bf5a69bd61e7)

The below tables show the activities and social signals to be classified along with
their respective accuracies. The accuracies were measured as the average recall for 
each class in 5-fold cross validation. The "Other" social signal class
includes laughing, talking, eating and singing. 

| Activity                | Accuracy |
|-------------------------|----------|
| Ascending stairs        | 96.13    |
| Descending stairs       | 96.43    |
| Sitting/Standing        | 99.06    |
| Shuffle walking         | 94.43    |
| Miscellaneous movements | 91.50    |
| Normal walking          | 97.83    |
| Lying on back           | 97.17    |
| Lying on left           | 96.12    |
| Lying on right          | 96.96    |
| Lying on stomach        | 98.98    |
| Running                 | 99.75    |

| Social Signal    | Accuracy |
|------------------|----------|
| Breathing normal | 86.83    |
| Coughing         | 91.10    |
| Hyperventilating | 81.37    |
| Other            | 90.41    |


# Application Structure
The application is designed with a modular structure. Each module is responsible for a specific 
functionality. For example, `ClassificationService` in the `services` 
directory performs background classification of activities and social signals using machine 
learning models. The important modules in the application and their functions are shown
in the table below. 

| Layer                | Module Name             | Function                                                                 |
|----------------------|-------------------------|--------------------------------------------------------------------------|
| **Presentation Layer** | MainActivity           | Central hub for navigation and app initialization.                      |
|                      | LiveDataActivity        | Real-time sensor data visualization and displaying classification results. |
|                      | HistoricalActivity      | Displays historical activity and social signal data for user-selected dates. |
| **Service Layer**    | ClassificationService   | Performs background classification of activities and social signals using machine learning models. |
|                      | BluetoothSpeckService   | Manages Bluetooth connectivity with the Respeck device.                 |
|                      | RespeckPacketDecoder    | Decodes raw Respeck data packets into usable accelerometer data.         |
|                      | RespeckPacketHandler    | Processes decoded data for use in visualization and classification.      |
| **Data Layer**       | AppDatabase             | Implements a Room database for persisting historical classification results. |
|                      | ActivityLogDao          | Provides methods for managing activity classification records in the database. |
|                      | SocialSignalLogDao      | Provides methods for managing social signal classification records in the database. |
|                      | DailyActivityLog        | Primary table for storing daily activity classification records.         |
|                      | DailySocialSignalLog    | Primary table for storing daily social signal classification records.    |
|                      | DataCleanupWorker       | Periodically deletes old database entries to optimize storage.           |



# Installing the App
The Actify app can be installed using Android Studio. Connect the mobile device to your computer 
via USB, ensure developer mode is enabled on the mobile device,
and deploy the app directly from Android Studio. Pair the Respeck sensor with the mobile device via 
Bluetooth before starting the app. 
Once connected, the app will begin collecting data from the Respeck.
