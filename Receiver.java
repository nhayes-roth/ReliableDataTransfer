
/*
 * File: Receiver.java
 * ------------
 * Name:       Nathan Hayes-Roth
 * UNI:        nbh2113
 * Class:      Computer Networks
 * Assignment: Programming Assignment #2
 * ------------
 * The receiver uses the receiving services of the TCP-like protocol to construct
 * the file sent by the sender. The receiver is invoked as follows:
 *      % Receiver file.txt 20000 128.59.15.37 20001 logfile.txt
 *                filename list_port remote_ip remote_port  log_filename
 * The receiver receives data on the listening_port, writes it to the specified file (filename)
 * and sends ACKS to the remote host at the remote_ip and remote_port. The receiver logs the
 * headers of all received and sent packets to log_filename and orders them by timestamp. 
 * Specifying log_filename as "stdout" will cause the log to display on standard output. The
 * log format is as follows:
 *      timestamp, source, destination, Sequence #, ACK#, and flags
 */

import java.io.*;
import java.net.*;

public class Receiver {
    


    /* Class Variables */
    
    
    /* main */
    public static void main(String[] args) {
        check(args); 
    }

    /* 
     * Check the command line arguments for proper form.
     */
     private static void check(String[] args){
         if(args.length != 5)
             chastise();
         else try{
             int port = Integer.parseInt(args[1]);
             port = Integer.parseInt(args[3]);
             InetAddress ip = InetAddress.getByName(args[2]);
         } catch (Exception e) {
             e.printStackTrace();
             chastise();
         }
     }

     /* 
      * Instruct the user how to properly execute the program.
      */

      private static void chastise(){
          System.out.println("\nImproper command format, please try again.");
          System.out.println("java Receiver [filename] [listening_port] " +
                             "[remote_ip] [remote_port] [log_filename]\n");
          System.exit(1);
      }

     /*
      * Generate a packet's header given a source_port, dest_port, and next_exp_seq number).
      * Note: because this is a one way data transfer, only the acknowledgment number matters;
      * the sequence number from the receiver is irellevant.
      */
      private static byte[] makeHeader(int source, int dest, int exp_seq){
         byte[] header = new byte[20];
         byte[] sourcePort = new byte[4];
         return header;
     }

    /*
     * Converts an integer to 16 bits (2 bytes), 
     * useful for Source and Dest port #'s.
     */
     private static byte[] intToTwo(int number){
         byte[] bytes = new byte[2];
         bytes[0] = (byte)(number >>> 8);
         bytes[1] = (byte)number;
         return bytes;
     }


    /*
     * Converts an integer to 32 bits (4 bytes), 
     * useful for sequence and acknowledgement numbers.
     */
     private static byte[] intToFour(int number){
         byte[] bytes = new byte[4];
         bytes[0] = (byte)(number >>> 24);
         bytes[1] = (byte)(number >>> 16);
         bytes[2] = (byte)(number >>> 8);
         bytes[3] = (byte)number;
         return bytes;
     }
}
