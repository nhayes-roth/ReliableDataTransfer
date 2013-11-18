# compiler
JCC = javac

# compilation flags
JFLAGS = -g

# default target
all: Sender.class Receiver.class

Sender.class: Sender.java
	$(JCC) Sender.java

Receiver.class: Receiver.java
	$(JCC) Receiver.java

.PHONY: clean
clean: 
	rm -f *.class
