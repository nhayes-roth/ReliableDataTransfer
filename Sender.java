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
import java.util.*;
import java.security.MessageDigest;

public class Sender implements Runnable{

        /* Class Variables */
        private static byte[] file_bytes;                                              // file of interest converted to bytes
        private static DatagramSocket socket;                                            // socket for sending and receiving data
        private static BufferedWriter log_writer;                                             // handles writing to log
        private static Queue<DatagramPacket> awaiting_ack = new LinkedList<DatagramPacket>(); // hold packets for potential re-send
        private static long timer = 0;                                                        // timeout timer (ms)
        private static long timeout = 500;                                                    // dynamically changing timeout time (ms)
        private static Hashtable<Integer, Long> times = new Hashtable<Integer, Long>();       // departure times for packets (ms)
        private static int packet_number = 0;                                                 // sequence number
        private static int packets_needed;
        private static int next_ack = 0;
        // args[]
        private static String filename;
        private static InetAddress remote_ip;
        private static int remote_port;
        private static int ack_port;
        private static int window_size;                                                   // number of packets allowed in queue
        private static String log_filename;
        
        
        /* Main */
        public static void main(String[] args) throws Exception {
                check(args);
                loadFile(filename);
                System.out.println("####### Bytes read in -- " + file_bytes.length);
                packets_needed = file_bytes.length/(256 - 20) + 1;
                socket = new DatagramSocket(ack_port);
                log_writer = Receiver.startLog(log_filename);
                send();
        }
        
        /*
         * Check the command line arguments for proper form, setting class variables if they do.
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
                else if (Integer.parseInt(args[4])<=0){
                        System.err.println("\nImproper window size. Please choose an integer value greater than 0.");
                        System.exit(1);
                }
                else {
                        filename = args[0];
                        try {
                                remote_ip = InetAddress.getByName(args[1]);
                                remote_port = Integer.parseInt(args[2]);
                                ack_port = Integer.parseInt(args[3]);
                                window_size = Integer.parseInt(args[4]);
                                log_filename = args[5];
                        } catch (Exception e) {
                                System.err.println("\nError parsing arguments.\n");
                                e.printStackTrace();
                                System.exit(1);
                        }
                }
        }
        
        /*
         * Converts the file into a byte[] for transmission.
         */
        private static void loadFile(String filename){
                File file = new File(filename);
                file_bytes = new byte[(int)file.length()];
                try {
                        FileInputStream stream = new FileInputStream(file);
                        stream.read(file_bytes);
                        stream.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError converting file into bytes.\n");
                        System.exit(1);
                }
        }

        /*
         * Manages the sending of data packets and receptions of ACKs.
         */
        private static void send() {
                try {
                        // start a thread to send packets
                        new Thread(new Sender()).start();
                        // listen for acks
                        while (true) {
                                byte[] buffer = new byte[256];
                                // receive ACK packet
                                DatagramPacket ack_packet = new DatagramPacket(buffer, buffer.length);
                                socket.receive(ack_packet);
                                // log it
                                System.out.print("RECEIVED ACK - "); //TODO: remove
                                logAckPacket(ack_packet);
                                // restart timer
                                timer = System.currentTimeMillis();

                                // shift window if seq_num matches
                                int ack_seq_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 4, 8));
                                if (ack_seq_num == next_ack){
                                        awaiting_ack.poll();
                                        next_ack ++;
                                }

                                // if an ACK with fin_flag true is received, we're done     
                                byte[] flags = Arrays.copyOfRange(buffer, 13, 14);
                                boolean fin_flag = (Boolean) (flags[0] == (byte) 1); 
                                if (fin_flag) {
                                        System.out.println("\nDelivery completed successfully.\n");
                                        log_writer.close();
                                        System.exit(1);
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                
        }

        /*
         * Write a log entry to the previously determined logfile, adding RTT estimate.
         */
        private static void logAckPacket(DatagramPacket ack_packet) throws UnknownHostException {
                byte[] buffer = ack_packet.getData();
                // extract the header fields
                int seq_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 4, 8));
                int ack_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 8, 12));        
                byte[] flags = Arrays.copyOfRange(buffer, 13, 14);
                boolean fin_flag = (Boolean) (flags[0] == (byte) 1); 
                String entry = "Time(ms): " + System.currentTimeMillis() + " ";
                entry += "Source: " + remote_ip.getHostAddress() + ":" + remote_port + " ";
                entry += "Destination: " + InetAddress.getLocalHost().getHostAddress() + ": " + ack_port + " ";
                entry += "Sequence #: " + seq_num + " ";
                entry += "ACK #: " + ack_num + " ";
                entry += "FIN: " + fin_flag + " ";
                // RTT
                long RTT = -1;
                if (times.containsKey(seq_num))
                        RTT = System.currentTimeMillis() - times.get(seq_num);                if (RTT >= 0){
                        entry += "RTT(ms): " + RTT + "\n";
                } 
                else {
                        entry += "RTT(ms): NA" + "\n";
                }
                try {
                        log_writer.write(entry);
                        log_writer.flush();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError encountered writing to logfile.\n");
                }
        }

        /*
         * Write a log entry to the previously determined logfile, just the packet.
         */
        private static void logSentPacket(DatagramPacket packet) throws UnknownHostException{
                byte[] buffer = packet.getData();
                // extract the header fields
                int seq_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 4, 8));
                int ack_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 8, 12));
                byte[] flags = Arrays.copyOfRange(buffer, 13, 14);
                boolean fin_flag = (Boolean) (flags[0] == (byte) 1); 
                String entry = "Time(ms): " + System.currentTimeMillis() + " ";
                entry += "Source: " + InetAddress.getLocalHost().getHostAddress() + ":" + ack_port + " ";
                entry += "Destination: " + remote_ip.getHostAddress() + ":" + remote_port + " ";
                entry += "Sequence #: " + seq_num + " ";
                entry += "ACK #: " + ack_num + " ";
                entry += "FIN: " + fin_flag + "\n";
                try {
                        log_writer.write(entry);
                        log_writer.flush();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError encountered writing to logfile.\n");
                }
        }
        
        /*
         * Independent thread to handle sending of data packets.
         */
        public void run(){
                try {
                        while (true) {
                                if (canSendMore()) {
                                        // make a new packet, send it, start the RTT timer, add to queue, log it
                                        DatagramPacket packet = makePacket();
                                        System.out.print("SENT - "); //TODO: remove
                                        socket.send(packet);
                                        times.put(packet_number, System.currentTimeMillis());
                                        packet_number++;
                                        awaiting_ack.add(packet);
                                        logSentPacket(packet);
                                }
                                else if(timeout()){
                                        // reset the timer, double timeout, and send packets again
                                        System.out.println("\t\t TIMEOUT at " + timeout);
                                        timer = System.currentTimeMillis();
                                        timeout = timeout*2;
                                        sendAgain();
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        // print error
                }
        }

        /* 
         * Constructs the next appropriate data packet for delivery.
         */
        private static DatagramPacket makePacket() {
                DatagramPacket packet = null;
                // grab relevant file_bytes
                int start_index = packet_number*236;
                int end_index = Math.min((packet_number+1)*236, file_bytes.length);
                byte [] data = Arrays.copyOfRange(file_bytes, start_index, end_index);
                byte [] all;
                try {
                        // calculate checksum
                        MessageDigest digest = MessageDigest.getInstance("MD5");
                        digest.update(data);
                        byte[] digest_bytes = digest.digest();
                        byte[] checksum = Arrays.copyOfRange(digest_bytes, 0, 2);
                        
                        // check if this is the last byte, set the flag
                        int fin_flag = 0;
                        if (packet_number == packets_needed - 1)
                                fin_flag = 1;

                        // append the header to the data
                        all = concat(Receiver.intToTwo(ack_port),
                                        concat(Receiver.intToTwo(remote_port),
                                        concat(Receiver.intToFour(packet_number),
                                        concat(Receiver.intToFour(next_ack),
                                        concat(Receiver.intToTwo(fin_flag),
                                        concat(Receiver.intToTwo(1),
                                        concat(checksum,
                                        concat(Receiver.intToTwo(0), data))))))));
                        
                        // construct packet
                        packet = new DatagramPacket(all, all.length, remote_ip, remote_port);
                } catch (Exception e){
                        e.printStackTrace();
                        System.err.println("\nError constructing packet.\n");
                }
                return packet;
        }
        
        /*
         * Concatenate two byte[] together.
         */
        private static byte[] concat(byte[] a, byte[] b){
                return Receiver.concat(a, b);
        }

        /*
         * Check the timer to see if a re-send is necessary.
         */
        private boolean timeout() {
                if (System.currentTimeMillis()-timer > timeout)
                        return true;
                else return false;
        }
        
        /*
         * Sends all the packets in the queue again.
         */
        private void sendAgain() {
                for (DatagramPacket packet : awaiting_ack){
                        try {
                                System.out.print("SENT AGAIN - "); //TODO: remove
                                logSentPacket(packet);
                                socket.send(packet);
                        } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("\nError sending packets from queue.\n");
                        }
                }
        }
        
        /*
         * Check to see if sending more packets is allowed.
         */
        private boolean canSendMore() {
                // is the window full
                if (awaiting_ack.size() == window_size) {
                        return false;
                } 
                // has the last packet been sent
                if (packet_number >= packets_needed) {
                        return false;
                }
                else return true;
        }
}