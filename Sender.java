/*
 * File: Sender.java
 * ------------
 * Name:       Nathan Hayes-Roth
 * UNI:        nbh2113
 * Class:      Computer Networks
 * Assignment: Programming Assignment #2
 * ------------
 * The sender sends the data in the specified file (filename) to the remote host at the
 * specified IP address (remote_IP) and port number (remote_port). The sender is invoked as follows:
 *      $ java Sender file.txt 128.59.15.38 20000 20001 1152 logfile.txt
 *                    filename, remote_IP, remote_port, ack_port_number, window_size, log_filename
 * In the above example the remote host (which can be either the receiver or the link emulator proxy) 
 * is located at 128.59.15.38 and port 20000. The command-line parameter ack_port_number
 * specifies the local port for the received acknowledgements. The window_size is
 * measured in terms of the number of packets and your sender should support values
 * from 1-65535. As before a log filename is specified. The log entry output format should
 * be similar to the one used by the receiver, however, it should have one additional output
 * field (append at the end), which is the estimated RTT. At the end of the delivery the
 * sender should indicate whether the transmission was successful, and print the number
 * of sent and retransmitted segments. The sender should report file I/O errors (e.g., ‘file
 * not found’).
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.security.MessageDigest;

public class Sender implements Runnable{

        /* main */
        public static void main(String[] args) throws Exception {
                // check form of args
                check(args);
                // parse args
                String filename = args[0];
                String remote_ip = args[1];
                InetAddress remote_address = InetAddress.getByName(remote_ip);
                int remote_port = Integer.parseInt(args[2]);
                int ack_port = Integer.parseInt(args[3]);
                int window_size = Integer.parseInt(args[4]);
                String log_filename = args[5];
                // start log
                BufferedWriter log_writer = startLog(log_filename);
        }

        /*
         * Check the command line arguments for proper form.
         */
        private static void check(String[] args) {
                // check length
                if (args.length != 6){
                        System.err.println("\nImproper command format, please try again.");
                        System.err.println("java Sender [filename] [remote_IP] [remote_port] "
                                         + "[ack_port_number] [window_size] [log_filename]\n");
                        System.exit(1);
                }
                // window size
                if (Integer.parseInt(args[4])<=0){
                        System.err.println("\nImproper window size. Please choose an integer value greater than 0.");
                        System.exit(1);
                }
        }

        /*
         * Open a BufferedWriter to the provided log_filename.
         */
        private static BufferedWriter startLog(String log_filename) {
                // write to standard out
                if (log_filename.equals("stdout")) {
                        return new BufferedWriter(new OutputStreamWriter(System.out));
                }
                // write to a specific file
                else {
                        try {
                                File file = new File(log_filename);
                                if (!file.exists()) {
                                        file.createNewFile();
                                }
                                return new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
                        } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("\nError encountered creating logfile\n");
                                System.exit(1);
                        }
                }
                return null;
        }
        
        /*
         * Override abstract method run()
         */
        public void run(){
        }
}