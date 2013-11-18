# Networks - Chatroom

- Nathan Hayes-Roth
- CSEE 4119 - Computer Networks
- Programming Assignment 1 - A simple client-server program


### Program Description

This application implements a simple client-server program similar to a chatroom. Users can log themselves into the server using the terminal and, once logged in, can communicate with the server and other logged in users by using a set of installed commands. 


### Development Information

- Programming Language: Java 
- Language Version: 	1.6.0_27
- Operating System:		Windows 7 	
- Software: 			Sublime Text 2 & cmd.exe


### Instructions to Compile and Run

1. Enter the project director
	
	`~/$ cd ~/.../nbh2113`

2. Compile all .java files.

    `~/.../nbh2113$ javac *.java`

3. In one terminal, start the server.

    `~/.../nbh2113$ java Server`
    	
    or
    
    `~/.../nbh2113$ java Server 4119`

3. In another terminal, start the Client.

	`~/.../nbh2113$ java Client`
	
	or
	
	`~/.../nbh2113$ java Client localhost 4119`


### Files

- ./credentials.txt
- ./README.md
- ./Client.java
- ./ClientThread.java
- ./Server.java


### Extensions

- threadsafe
- credentials stored in private hashtables to prevent unauthorized access
- prevent multiple clients from logging in with the same credentials
- /help and /quit commands
- inform users when a new user join/leaves the server
- wholasthr includes time since last login
- broadcast messages include sender name


### Assignment Instructions

Programming Assignment 1 

1. Introduction 
In this assignment we ask you to implement a simple client-server program that users could login themselves to the server using client terminal. Server should be able to respond to some simple commands from logged-in user, and should also be able to broadcast message to all logged-in users.

2. Specification 
a) Basically you should write a server program and a client program. They will be run from the terminals on the same host and obviously running on different ports. 

b) Client program should be able to support following functionalities: 
i) User should be able to connect to the server and login themselves by entering their username and password; 
ii) After logging in, user should be able to give the server commands. Your program should be able to display what the server responds at the terminal. 
iii) Server may broadcast message to all logged in users at any time. Your program should display these message. However you may assume that the server won't be broadcasting message when your user is inputting new commands. 
iv) When all work is done, user should be able to logout from the server. 

c) Server Specification
i) When starting up, server reads in a list of username-password combinations when start up. And then listens on a given port, waiting for users to connect; 
ii) When a new user comes, ask him/her to put in the username and password. If the combination is correct, log him/her in and prompt with welcome message. If the password is incorrect, it should ask the user to try again until that fails for 3 times. In this case, the server should drop this connection and block access on this port from the failed attempt IP address for 60 seconds.
iii) After the user logged in, the server will start responding to commands given by the user. If it cannot recognize some command, error message should be delivered.

3. Deliverable: 
You should submit source code, makefile, and a document stating in what part your program behaves differently compared to the specification. 

4. Testing, Grading, and Sample Run 
TA's will compile the code you submit, run the server program, run the client program. And then starts testing your program. We are asking you to write a makefile because different students have different ways to organize their code. It's almost impossible for us to figure out how to run your code first and judging your solution.

We will observe the functionality and correctness of your code. But also we will examine your source code a bit. Too poorly structured code or human-unreadable code may result in a score penalty. 

Here is a sample run showing how we will be testing your code: 

at terminal 1: 
```
make
java server 4119				// 4119 here is just an example. We may test on other unoccupied ports
```

at terminal 2: 
```
java client 10.11.12.13 4119	// 4119 is the port that server listens on, and 10.11.12.13
								// is the IP address of the server program 
Username: network				// after connecting to server, your program should prompt "Username: " and 
								// wait for user to put in his/her name. Here our user is "network" 
Password: seemsez				// again your program should prompt "Password: " and wait for user to put in 
								// the password. Since this is a introductory assignment, the password 
								// displayed as plain text is acceptable 
Welcome to simple server		// welcome message from the server acknowledging that the user has logged in 
Command:						// from now on your server should respond to user's command as defined in appendix 
```

5. Appendix 
b) Username-password list: 
Columbia 116bway 
SEAS summerisover 
csee4119 lotsofexams 
foobar passpass 
windows withglass 
Google hasglasses 
facebook wastingtime 
wikipedia donation 
network seemsez

a) Commands that server should support: 

| command     | functionality                                                  |
| ---         | ---                                                            |
| `whoelse`   | displays name of other connected users                         |
| `wholasthr` | displays name of users that connected within the last hour     |
| `broadcast` | broadcast message following this command to all connected user |