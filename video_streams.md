## Problem statement
An video sensor released by an OTT company writes bitrate and framerate values to 2 separate UDP socket ports.
  - on socketA it writes bitrate updates like this:
```
      {video_player: <UUID-X>, bitrate: 3500, utc_minute: 1699520400}
      {video_player: <UUID-Y>, bitrate: 4500, utc_minute: 1699520400}
      ...
      {video_player: <UUID-X>, bitrate: 3520, utc_minute: 1699520460}
      ...
      {video_player: <UUID-Y>, bitrate: 4600, utc_minute: 1699520460}
      ...
```
  - on socketB it writes framerate updates like this:
```
      {video_player: <UUID-X>, framerate: 24, utc_minute: 1699520400}
      {video_player: <UUID-Y>, framerate: 30, utc_minute: 1699520400}
      ...
      {video_player: <UUID-X>, framerate: 26, utc_minute: 1699520460}
      ...
      {video_player: <UUID-Y>, framerate: 28, utc_minute: 1699520460}
      ...
```

Build an application that can consume data from both these streams to show consolidated values which is printed to the log/console. For example

```
$ sensors:
video_player <UUID-X> is at bitrate 3500 and framerate 24 at 1699520400
video_player <UUID-Y> is at bitrate 4500 and framerate 30 at 1699520400
...
video_player <UUID-X> is at bitrate 3520 and framerate 26 at 1699520400
...
video_player <UUID-Y> is at bitrate 4600 and framerate 28 at 1699520400
...
```

Due to vagaries of the network, some of messages in can be out of sequence wrt time (later bitrate can come earlier). But a singe output line for a given timestamp should be printed for a sensor only after both values are received.

Assume this to be a business problem and use sensible defaults and assumptions. You can make things configurable when you dont know the technical answer to something. But do not expect more info on this problem from the business folk. Push the code to Github and provide the repo link. A short description of the design and how to run the program will be highly appreciated (but not necessary). You can use any programming language and any library/framework - no restrictions at usage of anything that is open source