# Application Structure
The application is designed with a modular structure. Each module is responsible for a specific functionality. For example, `ClassificationService` in the `services` 
directory performs background classification of activities and social signals using machine learning models. For a detailed overview of the application structure, please refer 
to Section 3.4 of the report. 

# Running Machine Learning notebook
The machine learning notebook can be run from the `Submission Folder\ML models` directory. Before attempting to run the notebook, ensure that the relevant libraries listed 
under imports are installed. The notebook then may simply be ran in VSCode or Jupyter Notebook.

# Installing the App
The Actify app can be installed using Android Studio. Connect the mobile device to your computer via USB, ensure developer mode is enabled on the mobile device,
and deploy the app directly from Android Studio. Pair the Respeck sensor with the mobile device via Bluetooth before starting the app. 
Once connected, the app will begin collecting data from the Respeck.
