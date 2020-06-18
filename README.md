# MessMe

MessMe is a messaging app. Users (clients) can join in on the server, running in the network, and start messaging/calling each other, using their preferred client type.

Now built for android.

Check the [Releases](https://github.com/lalithkota/MessMe/releases) too. (It is an extension of [Assignment 1](https://github.com/lalithkota/RTOS/tree/master/Assignment1) of RTOS, where a client-server chat program was built using C and sockets)

Just to make it clear, this is an incomplete version.
Video-call and Group-audio-call are not properly working/implemented.
(Even ending a normal one-to-one-audio-call is not properly working/implemented.)

(A dockerimage is tried to be built, but abandoned later.)

## 1. How to Use

### 1a. Prerequisites

- CLI-client uses ncurses library, for the terminal gui.
- CLI-client uses pulseaudio (and pulse, pulse-simple libraries) for audio related purposes.
- CLI-client uses opencv for video related purposes.
- CLI-client can be built & used without opencv/video-features.
- Server & CLI-client also use pthread library.

### 1b. Building/Compiling

- Just a `make`, builds all server and CLI-client-with-video & without-video, also clearing the bins
- Just a `make vid`, builds both server and CLI-client-with-video
- Just a `make novid`, builds both server and CLI-client-without-video
- Or:
  - To only build the server:
    - Use `make server`
  - Only the CLI-client-with-video:
    - Use `make client_vid`
  - Only the CLI-client-without-video:
    - Use `make client_novid`
- For the android client:
  - Install and use the latest version from the [Releases](https://github.com/lalithkota/MessMe/releases).

### 1c. Running

- For running server, use
  - `bin/server <Interface-IP-Addr> <Desired-port-no>`
- For CLI-client-with-video, use
  - `bin/client_vid <server-IP-Addr> <server-port-no>`
- For CLI-client-without-video, use
  - `bin/client_novid <server-IP-Addr> <server-port-no>`
- Android(?), more obvious.

Happy messaging.

## 2. Usage (CLI-Client Commands)

- First type in the login details as prompted.
- Now on, any message that you type will be treated as one that you want to send to your chosen receiver.
- Except messages starting with '-'. (Say, they are more like messages to server.)
- Here is the full list, how to use them, what they mean.
  - `-q` to exit the server.
  - `-l` to list all the users and groups, with theirs names and usernames.
  - `-s <receiver-name>` to *select* the desired receiver(the person u want to talk to) using their __*name*__.
  - `-su <receiver-username>` to *select* the desired receiver using their __*username*__.
  - `-sr` to clear *selected* receiver (Meaning, after this no receiver will be selected).
  - `-o` to see your *selected* receiver's name.
  - `-c` to change your name.
  - `-g <group-username>` to *select* the group you want to talk to, using its __*username*__.
  - `-gn <new-group-name>` to create a new group, with the given __*name*__.
  - `-ga <person-uname>` to add a person with that __*username*__ to the *selected* group.
  - `-gl` to list the members of the *selected* group.
  - `-gc <new-group-name>` to change the name of the *selected* group.
  - `-gr` to leave the *selected* group.
  - `-n` to shift the terminal 2 lines up.
  - `-n<x>` to shift the terminal x lines up (`-n1` to shift one line up. `-n2` to shift 2 lines up, etc).
  - `-r` to clear the whole page on terminal.
  - `-v` to initiate an audio call, with the *selected* receiver.
  - `-vi` to initiate a video call, with the *selected* receiver.
- Just to assert, for any operation on a receiver, you need to *select* the receiver first. Then the corresponding operation/message can be delivered to the *selected* receiver.

## 3. A Lot Of Notes

#### Notes v1.0:

- The program, on some level, treats groups and users alike. So group names have a ':' appended before them to distinguish them from normal users. (Use `-l` after creating a group to better understand this.)
- So, groups can be members of other groups themselves. Though, as of now, nothing recursive is programmed to happen when messaged.
- You can list all the groups present on the server. But can only select (`-g`) the ones that you are present in.
- New Group creation will already add you as the first member in the group. It will also clear your previous selection of talking. Group or person. (`-sr` will also clear the current selection of talking. Group or person.)

#### Notes v1.2:

<details><summary>User-Base system - Centralized/Decentralized</summary>

  - The application once employed a centralized user-base system. (Meaning the user-related info of other users was stored only in the server).
  - It now became decentralized. Meaning every other client gets all the info about your joinings, name-changes (yes there is that too), the user-quits, etc.
  - So that it can manage its own info and gui. (Idea being; each client maintains its own database of users) (CLI-client doesn't do anything with that info for now.)
</details>

<details><summary>User's identity - Username</summary>

  - Previously a user's identity was solely based on his name, which felt foolish,
  and a new aspect of Username is added which is unique for each user.
  - So that the name can be whatever it wants, and that there can also be same named clients.
  - All the previous operations that are done using the name, are changed so that they are done using the username.
    - Except for `-s`. See above usage. It selects the first user with this name from the database.
    - [ ] Using `-o` only shows the name of the receiver. Not adapted to username yet.
</details>

<details><summary>Sent & Received Message Headers</summary>

  - Previously when the sender sends a message, the server used to append the name of the sender at the beginning and then send the message over to the receiver.
  - But now it is changed to use the username, assuming that every client has a database of the user-info and can take care of it.
  - The android-client does that already, but the CLI-client doesn't (yet). It simply displays the message raw on screen.
</details>

<details><summary>Groups and Username</summary>

  - When a group is created. There is no control over its username. It is just set to the name itself, mostly.
  - When such group with that username already exists a char is appended and tried again, retaining the idea that username is unique.
</details>

<details><summary>Once the username concept is involved, a password concept is also implemented, to create a login interface. So that a user can log back into his own username the next time.</summary>

</details>

<details><summary>On android, group creation, group member addition, name changing, all these are implemented. See top right options.</summary>

</details>

<details><summary>On android, unread messages feature is also implemented.</summary>

  - A simple scroll lock is also implemented so that once the user scrolls up (the scroll lock is lifted) all new messages received, go unread.
  - When the user gets such a message the scroll is NOT disturbed back to bottom.
  - But if the user scrolls down or if the scroll is at the end (the scroll is locked), and all new messages go read directly, and a new message bottom-fits itself.
</details>

<details><summary>Audio Calling is implemented.</summary>

  - Pulseaudio is used to implement it in C.
  - Even in android, the audio encoding, bit depth, no of channels, are same for both the CLI-client and the android-client.
  - So seamless calling can be made from the CLI-client to the android-client.
</details>

<details><summary>Pickup/Hangup feature is also implemented. But the CLI-client (and only the CLI-client) is not quitting "gracefully". Meaning, soon as client-1 quits, client-2 crashes. Here are the details:</summary>

  - Calls are maintained on a different socket than the usual messaging one. So on each client, atleast two threads are running for the call. One which records and sends to server, and one which reads from server and plays out.
  - If a client wants to quit the call, it basically closes its end of the socket.
  - Server now realizes that, and closes its end of the sockets too.
  - But server has to close two sockets, one for each user. (so basically closes both the sockets) (considering only two people in the call)
    - [ ] Also group call is not implemented yet.
  - Now when the second user reads from the closed server socket, it gets EOF; and then realizes the other user quit the call. So now the second client ends/closes its socket and quits too.
  - Now, why is it crashing? I think during a read/write process in C, if it gets EOF, it returns properly with a zero. But now if you further do another read/write it causes the program to crash. Something of this nature is happening here.
  - On the server side (also being multithreaded for each user's call handling) atleast some measures are taken so that such a crash like the CLI one doesnt occur. But a rare chance is there, for that a more robust method is needed for handling the call-finish process.
</details>

#### Notes v1.4:

<details><summary>`-o` problem</summary>

  - [x] Result of `-o` changed. Adapted to username.
</details>

<details><summary>Port number was hardcoded in android before now it is changed.</summary>

  - Username, password, name, previously-successful-ip & port, are all stored in a file.
  - Whenever the app opens it try to connect with the details from that file.
  - If it fails it prompts for inputs again, accordingly.
</details>

<details><summary>A refined 3-channel server system is implemented.</summary>

  - Meaning, the normal chat is handled on a socket whose port number = `port_no`. (Given by user while running the server.)
  - Audio Calling is handled on socket whose port number = `port_no + 1`.
  - Video Calling is handled on socket whose port number = `port_no + 2`.
</details>

<details><summary>The above channels update was brought because:</summary>

  - previously on the server side, the socket creation happened, only after a client initialized the call with `-v*`.
  - So when a newer pair initiates the call, socket creation would fail.
  - Now that the channel system is more open, the socket creation for all the three sockets happens at the start of the server.
  - Also previously, call-receiver only connected to the corresponding call-socket-channel, after acknowledging whether accepting the call or not.
    - But now it is changed so that the receiver connects to the call-socket-channel immediately upon call-receival. And then they wait for acknowledgments.
</details>

<details><summary>Main update: Video-Calling is implemented. (CLI-client uses opencv.) (Android uses CameraAPI v1.) (All frame are only 640x480. Android is forced into landscape.)</summary>

  - CLI-client, on one thread,
    - captures one frame from camera.
    - converts to jpeg image. Gets byte-array from the image. Byte-array is of fixed length 40kib.
    - sends it over to the server.
  - CLI-client, on a second thread,
    - receives byte-array from server, of size 40kib.
    - decompress/decodes the byte-array from jpeg image type to normal frame.
    - displays the frame.
  - Android-client, using the CameraAPI's onPreviewFrame,
    - receives a PreviewFrame, where the frame data is of type YUV_NV21.
    - converts/compresses to JPEG. Gets the byte array, of fixed size, 40kib.
    - pushes the byte-array into a queue of unsent images.
  - Android-client, on one new thread,
    - sends each of the images on the unsent queue over to the server.
  - Android-client, on second new thread,
    - receives a byte-array from server, of size 40kib.
    - creates a Bitmap from bytearray.
    - puts the Bitmap on ImageView.
  - Server, on one new thread,
    - receives a 40kib byte-array from client-1.
    - puts the byte-array back to client-2.
  - Server, on second new thread,
    - receives a 40kib byte-array from client-2.
    - puts the byte-array back to client-1.
</details>

<details><summary>Problems with this version</summary>

  - Final received images are purely corrupt.
    - Most of the times they can be partially made out.
    - Android client displays no image at all.
      - Maybe updating the Bitmap on ImageView problem.
      - Or the Bitmap can't be created.
    - CLI-client can partially display the images it received from android.
      - The byte-arrays(fixed size), before sending on sender-client side, are written to a local jpeg file. Their quality is good. (Uncorrupted)
      - The byte-array, upon receival by receiever-client, are also written to a file locally. And they are corrupt.
  - [x] Initially, the jpeg byte-array size was not fixed, to conserve a lot of memory.
    - So, first a four byte size was sent before sending the actual byte-array.
    - But then, it was perceived to be the problem of corruption and had to be changed to fixed size (despite giving same results).
  - [x] The next problem-perspective was that,
    - a new frame is being prematurely written to server, before the old one is done being written.
    - Hence, the queuing was introduced.
    - But it didn't change the outcome.
  - [x] Then the next problem-perspective was that,
    - tcp buffers are being overrun.
    - But the protocol inherently needs to take care of it.
    - But still,
      - a method was implemented, where the byte-array is transmitted in terms of 1kib parts with added delays between each subtransmission. And the transmissions were also received in the same way.
      - But in vain. Same outcome.
  - [ ] Still don't know whats causing it.
</details>
