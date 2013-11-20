// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE
// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE
// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE
// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE
// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE// THIS ISN"T THE REAL ONE

import java.io.*;
import java.net.*;
import java.nio.*;
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
        private static int packet_number;
        private static int packets_needed;
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
                        long eRTT = 0;  // will change over time
                        // start a thread to send packets
                        new Thread(new Sender()).start();
                        // listen for acks
                        while (true) {
                                byte[] buffer = new byte[256];
                                // receive ACK packet
                                DatagramPacket ack_packet = new DatagramPacket(buffer, buffer.length);
                                socket.receive(ack_packet);
                                // separate header from data
                                byte[] Header = Arrays.copyOfRange(buffer, 0, 20);
                                byte[] data = Arrays.copyOfRange(buffer, 20, buffer.length);  // should be empty
                                // extract the header fields
                                int source_port = Receiver.toInteger(Arrays.copyOfRange(buffer, 0, 2));
                                int dest_port = Receiver.toInteger(Arrays.copyOfRange(buffer, 2, 4));
                                int seq_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 4, 8));
                                int ack_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 8, 12));
                                int header_len = Receiver.toInteger(Arrays.copyOfRange(buffer, 12, 13));
                                byte[] flags = Arrays.copyOfRange(buffer, 13, 14);
                                boolean fin_flag = (Boolean) (flags[0] == (byte) 1); 
                                byte[] rec_window = Arrays.copyOfRange(buffer, 14, 16);
                                byte[] checksum = Arrays.copyOfRange(buffer, 16, 18);
                                byte[] urgent = Arrays.copyOfRange(buffer, 18, 20);
                                
                                /* TODO: verify that it the correct ACK
                                 *    - check the seq_num compare it to the first awaiting_ack packet
                                 *    - if it's right, remove that packet/shift the window
                                 */
                                
                                
                                // calculate RTT and log ACK
                                long RTT = System.currentTimeMillis() - times.get(seq_num);
                                eRTT = RTT/(long)8 + eRTT * (long)7/(long)8;
                                log(remote_ip, source_port, InetAddress.getLocalHost(), dest_port, seq_num, ack_num, fin_flag, RTT);

                                // if an ACK with fin_flag true is received, we're done
                                if (fin_flag) {
                                        System.out.println("+----------------------------------<");
                                        System.out.println("|Delivery completed!");
                                        System.out.println("+----------------------------------<");
                                        log_writer.close();
                                        System.exit(1);
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                
        }

        /*
         * Write a log entry to the previously determined logfile, just the packet.
         */
        private static void log(DatagramPacket packet) throws UnknownHostException{
                byte[] buffer = packet.getData();
                // extract the header fields
                int source_port = Receiver.toInteger(Arrays.copyOfRange(buffer, 0, 2));
                int dest_port = Receiver.toInteger(Arrays.copyOfRange(buffer, 2, 4));
                int seq_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 4, 8));
                int ack_num = Receiver.toInteger(Arrays.copyOfRange(buffer, 8, 12));
                byte[] flags = Arrays.copyOfRange(buffer, 13, 14);
                boolean fin_flag = (Boolean) (flags[0] == (byte) 1); 
                String entry = "Time(ms): " + System.currentTimeMillis() + " ";
                entry += "Source: " + InetAddress.getLocalHost().getHostAddress() + ":" + source_port + " ";
                entry += "Destination: " + remote_ip.getHostAddress() + ": " + dest_port + " ";
                entry += "Sequence #: " + seq_num + " ";
                entry += "ACK #: " + ack_num + " ";
                entry += "FIN: " + fin_flag;
                try {
                        log_writer.write(entry);
                        log_writer.flush();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError encountered writing to logfile.\n");
                }
        }
        
        /*
         * Write a log entry to the previously determined logfile, with explicit arguments.
         */
        private static void log(InetAddress remote_ip, int source_port,
                        InetAddress localHost, int dest_port, int seq_num,
                        int ack_num, boolean fin_flag, long RTT) {
                String entry = "Time(ms): " + System.currentTimeMillis() + " ";
                entry += "Source: " + remote_ip.getHostAddress() + ":" + source_port + " ";
                entry += "Destination: " + localHost.getHostAddress() + ": " + dest_port + " ";
                entry += "Sequence #: " + seq_num + " ";
                entry += "ACK #: " + ack_num + " ";
                entry += "FIN: " + fin_flag + " ";
                entry += "RTT(ms): " + RTT;
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
                long calculated_timeout = 1;
                try {
                        while (true) {
                                if(timeout()){
                                        // reset the timer, double timeout, and send packets again
                                        timer = System.currentTimeMillis();
                                        timeout = timeout*2;
                                        sendAgain();
                                } else if (canSendMore()) {
                                        // make a new packet, send it, log it
                                        DatagramPacket packet = makePacket(); // TODO: this part, then fix the ack accepter in send()
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        // print error
                }
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
                                log(packet);
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