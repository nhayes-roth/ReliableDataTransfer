# Reliable Data Transfer

- Nathan Hayes-Roth
- CSEE 4119 - Computer Networks
- Programming Assignment 2 - Simple TCP-like transport-layer protocol


### Program Description

This package implements a one-way version of a reliable data transfer protocol, similar to that of TCP.
The programs guarantee reliable, in-order delivery of a stream of bytes, recovering from dynamic network
problems and problems such as packet loss, packet corruption, packet duplication, and packet reordering.

The package implements a version of Go-Back-N protocol, with dynamically changing timeout timers. In order
to use the package, the source code for the Sender and Receiver should be compiled and run on separate
machines (or terminals). Once compiled, the programs can be executed in the following manners:

##### Receiver
`~/.../nbh2113$ java Receiver result.txt 20000 localhost 20001 stdout
//command line exec with filename, listening_port remote_IP, remote_port, log_filename`

The receiver receives data on the listening_port, writes it to the specified file (filename) and sends ACKS 
to the remote host at the remote_ip and remote_port. The receiver logs the headers of all received and sent 
packets to log_filename and orders them by timestamp. Specifying log_filename as "stdout" will cause the log 
to display on standard output. The log format is as follows

##### Sender
`~/.../nbh2113$ java Sender test.txt localhost 20000 20001 5 stdout
\\command line exec with filename, remote_IP, remote_port, ack_port_number, window_size, log_filename`

In the above example the remote host is located at 128.59.15.38 and port 20000. The command-line parameter 
ack_port_number specifies the local port for the received acknowledgements. The window_size is measured in 
terms of the number of packets and your sender should support values from 1-65535. As before a log filename 
is specified. 

### Development Information

- Programming Language: Java 
- Language Version: 	OpenJDK 6
- Operating System      Ubuntu 12.04	
- Software: 		terminal


### Instructions to Compile and Run

1. Enter the project director.
	
	`~/$ cd ~/.../nbh2113`

2. Run Make to compile all the source code.

    `~/.../nbh2113$ make`

3. In one terminal, start the receiver.

    `~/.../nbh2113$ java Receiver result.txt 20000 localhost 20001 stdout`

4. In another terminal, start the sender.

    `~/.../nbh2113$ java Sender test.txt localhost 20000 20001 5 stdout`


### Files

- ./README.md
- ./Makefile
- ./Sender.java
- ./Receiver.java


### Assignment Instructions

- You will implement a one-way version of TCP without the initial connection 
  establishment, but with a FIN request to signal the end of the transmission.

- Sequence numbers should start from zero.

- You do not have to worry about congestion or flow control, and thus the sender 
  window size should be a configurable command-line specified fixed parameter.

- You should adjust your retransmission timer as per the TCP standard (although 
  it may be advisable to use a fixed value for initial experimentation)

- You need to implement the 20 byte TCP header format, without options.

- You do not have to implement push (PSH flag), urgent data (URG), reset (RST) or 
  TCP options.

- You should set the port numbers in the packet to the right values, but can 
  otherwise ignore them.

- The TCP checksum is computed over the TCP header and data; this does not quite 
  correspond to the correct way of doing it (which includes parts of the IP header), but is close enough.
