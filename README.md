##KorselControl
Korsel Control is a Android software to remotely control a Korsel robot via a smartphone's accelerometer.
Because of the simple protocol other Bluetooth enabled robots can easily be made compatible with this app.
The C-code for the Bluetooth Korsel can be cound in the folder KorselEmbeddedSoftwareNewKorselVersion.

###Communication protocol
The communication is done over Bluetooth serial protocol.
Each data package consists of two bytes:

| Byte 1  | Byte 2 |
|---------|--------|
| COMMAND | VALUE  |

These are the currently supported commands with their respecting possible values:

| MEANING                        | COMMAND | VALUE     |
|--------------------------------|---------|-----------|
| Set left motor forward speed   | 0x01    | [0..255] |
| Set left motor backward speed  | 0x02    | [0..255] |
| Set right motor forward speed  | 0x03    | [0..255] |
| Set right motor backward speed | 0x04    | [0..255] |
| Set line following mode        | 0x05    | [0,1]     |
| Auxiliary for custom usage     | 0x06    | 0         |
|                                |         |           |
| Photo sensor state             | 0x07    | [0..255]  |

All except the "Photo sensor state" are outgoing packages. While "Photo sensor state" is a package 
sent from the Korsel containing a value representing the current value of the photo sensor (0 means white, 255 black). 
