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
| Set left motor forward speed   | 0x01    | [11..255] |
| Set left motor backward speed  | 0x02    | [11..255] |
| Set right motor forward speed  | 0x03    | [11..255] |
| Set right motor backward speed | 0x04    | [11..255] |
| Set line following mode        | 0x05    | [0,1]     |
| Auxiliary for custom usage     | 0x06    | 0         |
|                                |         |           |
| Photo sensor state             | 0x07    | [0,1]     |

All except the "Photo sensor state" are outgoing packages. While "Photo sensor state" is a package sent from the Korsel containing 0 or 1 representing the current state of the photo sensor.
Values < 11 are reserved for commands. So PWM values below 11 are sent as 11.
