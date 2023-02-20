# File-Client-App

This is the client part of an Android File Client / File Provider POC to test the Android FileProvider service. The Client allows the app to scan their device for apps that offer file-sharing services. If such an app exists (users should install the sister app for this project, the 'File-Provider-App' on the same device or emulator), the user will be able to browse the available files by clicking the 'Get Files' button on the homescreen. If the file has the correct permissions and can be read, the client app will request the file, which is streamed via a ParcelFileDescriptor (PFD) / FileDescriptor. The PFD also carries the option to get the file name and size, but we do not utilize this here.

The contents are converted into a CharArray and displayed in an EditText on the home page. The user then has the option to save the contents into a new file on the Client data storage. The file name and location are hard-coded.
