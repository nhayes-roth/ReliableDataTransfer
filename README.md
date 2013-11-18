# Reliable Data Transfer

- Nathan Hayes-Roth
- CSEE 4119 - Computer Networks
- Programming Assignment 2 - Simple TCP-like transport-layer protocol


### Program Description

This program implements a one-way version of a reliable data transfer protocol, similar to that of TCP.

### Development Information

- Programming Language: Java 
- Language Version: 	1.6.0_27
- Operating System:		Windows 7 	
- Software: 			Sublime Text 2 & cmd.exe


### Instructions to Compile and Run

1. Enter the project director.
	
	`~/$ cd ~/.../nbh2113`

2. Run Make to compile all the source code.

    `~/.../nbh2113$ make

3. In one terminal, start the sender.

    `~/.../nbh2113$ make sender`
    	
    or
    
    `TODO: explicit startup`

3. In another terminal, start the receiver.

	`~/.../nbh2113$ make receiver`
	
	or
	
	`TODO: explicit startup`

4. (Optional) In another terminal start the proxy.


### Files

- ./README.md

### Extensions



### Assignment Instructions

-You will implement a one-way version of TCP without the initial connection
establishment, but with a FIN request to signal the end of the transmission.

-Sequence numbers should start from zero.

-You do not have to worry about congestion or flow control, and thus the sender window size should be a configurable command-line specified fixed parameter.

-You should adjust your retransmission timer as per the TCP standard (although it may be advisable to use a fixed value for initial experimentation)

-You need to implement the 20 byte TCP header format, without options.

-You do not have to implement push (PSH flag), urgent data (URG), reset (RST) or
TCP options.

- You should set the port numbers in the packet to the right values, but can otherwise ignore them.

-The TCP checksum is computed over the TCP header and data; this does not quite correspond to the correct way of doing it (which includes parts of the IP header), but is close enough.


