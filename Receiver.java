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
 *      $ java Receiver file.txt 20000 128.59.15.37 20001 logfile.txt
 *                     filename list_port remote_ip remote_port  log_filename
 * The receiver receives data on the listening_port, writes it to the specified file (filename)
 * and sends ACKS to the remote host at the remote_ip and remote_port. The receiver logs the
 * headers of all received and sent packets to log_filename and orders them by timestamp. 
 * Specifying log_filename as "stdout" will cause the log to display on standard output. The
 * log format is as follows:
 *      timestamp, source, destination, Sequence #, ACK#, and flags
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.security.MessageDigest;

public class Receiver {

        /* main */
        public static void main(String[] args) throws Exception {
                check(args);
                BufferedWriter log_writer = startLog(args[4]);
                byte[] bytes_received = receive(args[1], args[2], args[3],
                                log_writer);
                writeToFile(bytes_received, args[0]);
        }

        /*
         * Check the command line arguments for proper form.
         */
        private static void check(String[] args) {
                // check length
                if (args.length != 5)
                        chastise();
                // check types
                else {
                        try {
                                int port = Integer.parseInt(args[1]);
                                port = Integer.parseInt(args[3]);
                                InetAddress ip = InetAddress.getByName(args[2]);
                        } catch (Exception e) {
                                // let them know what they've done
                                e.printStackTrace();
                                chastise();
                        }
                }
        }

        /*
         * Instruct the user how to properly execute the program.
         */
        private static void chastise() {
                System.err.println("\nImproper command format, please try again.");
                System.err.println("java Receiver [filename] [listening_port] "
                                + "[remote_ip] [remote_port] [log_filename]\n");
                System.exit(1);
        }

        /*
         * Open a BufferedWriter to the provided log_filename.
         */
        public static BufferedWriter startLog(String log_filename) {
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
         * Receive data sent via UDP and construct the logfile.
         */
        private static byte[] receive(String listening_port, String remote_ip,
                        String remote_port, BufferedWriter log_writer)
                        throws Exception {
                int l_port = 0;
                int r_port = 0;
                InetAddress ip = null;
                // parse ports and ip
                try {
                        l_port = Integer.parseInt(listening_port);
                        r_port = Integer.parseInt(remote_port);
                        ip = InetAddress.getByName(remote_ip);
                }
                // shouldn't rech this and still throw an exception
                catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError parsing ports or remote ip address.\n");
                        System.exit(1);
                }
                byte[] bytes_received = null;
                DatagramSocket socket = new DatagramSocket(l_port);
                int expected_seq_num = 0;
                int response_seq_num = 0;
                boolean fin_flag = false;
                // loop until the fin_flag is received
                while (!fin_flag) {
                        // receive a packet of data up to size 256 Bytes
                        byte[] buffer = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buffer,
                                        buffer.length);
                        socket.receive(packet);
                        // separate header from data
                        byte[] Header = Arrays.copyOfRange(buffer, 0, 20);
                        byte[] data = Arrays.copyOfRange(buffer, 20,
                                        buffer.length);
                        // extract the header fields
                        int source_port = toInteger(Arrays.copyOfRange(buffer,
                                        0, 2));
                        int dest_port = toInteger(Arrays.copyOfRange(buffer, 2,
                                        4));
                        int seq_num = toInteger(Arrays
                                        .copyOfRange(buffer, 4, 8));
                        int ack_num = toInteger(Arrays.copyOfRange(buffer, 8,
                                        12));
                        int header_len = toInteger(Arrays.copyOfRange(buffer,
                                        12, 13));
                        byte[] flags = Arrays.copyOfRange(buffer, 13, 14);
                        fin_flag = (Boolean) (flags[0] == (byte) 1); // only
                                                                     // flag we
                                                                     // care
                                                                     // about
                        byte[] rec_window = Arrays.copyOfRange(buffer, 14, 16);
                        byte[] checksum = Arrays.copyOfRange(buffer, 16, 18);
                        byte[] urgent = Arrays.copyOfRange(buffer, 18, 20);
                        // write log entry for received packet
                        log(remote_ip, source_port, 
                            InetAddress.getLocalHost().getHostAddress(), dest_port, 
                            seq_num, ack_num, fin_flag, log_writer);
                        // validate the correctness of the received packet
                        if (validate(seq_num, expected_seq_num, checksum, data)) {
                                // add data to received
                                if (bytes_received == null) {
                                        bytes_received = data;
                                } else {
                                        bytes_received = concat(bytes_received,
                                                        data);
                                }
                                // change expected seq number and send ack
                                expected_seq_num += data.length;
                                byte[] ack = concat(intToTwo(dest_port),
                                             concat(intToTwo(source_port),
                                             concat(intToFour(response_seq_num),
                                             concat(intToFour(expected_seq_num),
                                             concat(Arrays.copyOfRange(buffer, 12, 13),
                                             concat(flags,
                                             concat(rec_window,
                                             concat(checksum,
                                             urgent))))))));
                                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, ip, source_port);
                                socket.send(ackPacket);
                                // write log entry
                                log(InetAddress.getLocalHost().getHostAddress(), dest_port, 
                                    ip.getHostAddress(), source_port, 
                                    response_seq_num, expected_seq_num, fin_flag, log_writer);
                                // ack is only a header, so increment the seq num
                                response_seq_num++;
                        }
                }
                // all packets received
                return bytes_received;
        }

        /*
         * Validates that the received packet is the expected one.
         */
        public static boolean validate(int actual, int expected, byte[] checksum, byte[] data) {
                // compare actual and expected seq_num
                if (actual == expected) {
                        return false;
                }
                // perform checksum
                try {
                        MessageDigest digest = MessageDigest.getInstance("MD5");
                        digest.update(data);
                        byte[] to_compare = digest.digest();
                        if (checksum[0] == to_compare[0] && checksum[1] == to_compare[1]) {
                                return true;
                        } else return false;
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nChecksum error encountered\n");
                }
                return false;
        }

        /*
         * Converts a byte array to an integer.
         */
        public static int toInteger(byte[] bytes) {
                // pad byte[2] to byte[4]
                if (bytes.length != 4) {
                        bytes = concat(new byte[2], bytes);
                }
                return ByteBuffer.wrap(bytes).getInt();
        }

        /*
         * Concatenate two byte arrays.
         */
        public static byte[] concat(byte[] first, byte[] second) {
                byte[] to_return = new byte[first.length + second.length];
                for (int i = 0; i < first.length; i++) {
                        to_return[i] = first[i];
                }
                for (int j = 0; j < second.length; j++) {
                        to_return[first.length + j] = second[j];
                }
                return to_return;
        }

        /*
         * Writes a log entry to the designated logfile.
         */
        private static void log(String source_ip, int source_port,
                                String dest_ip, int dest_port, 
                                int seq_num, int ack_num, boolean fin, 
                                BufferedWriter log_writer) {
                String entry = "Time(ms): " + System.currentTimeMillis() + " ";
                entry += "Source: " + source_ip + ":" + source_port + " ";
                entry += "Destination: " + dest_ip + ": " + dest_port + " ";
                entry += "Sequence #: " + seq_num + " ";
                entry += "ACK #: " + ack_num + " ";
                entry += "FIN: " + fin + " ";
                try {
                        log_writer.write(entry);
                        log_writer.flush();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError encountered writing to logfile.\n");
                }
        }

        /*
         * Reconstruct the original file and save it to the provided filename.
         */
        private static void writeToFile(byte[] bytes_received, String filename) {
                try{
                        FileOutputStream stream = new FileOutputStream(filename);
                        stream.write(bytes_received);
                        stream.close();
                        System.out.println("\n " + filename + " successfully received.");
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("\nError encountered creating output file.\n");
                }
                       return;
        }

        /*
         * Converts an integer to 16 bits (2 bytes), useful for Source and Dest
         * port #'s.
         */
        public static byte[] intToTwo(int number) {
                byte[] bytes = new byte[2];
                bytes[0] = (byte) (number >>> 8);
                bytes[1] = (byte) number;
                return bytes;
        }

        /*
         * Converts an integer to 32 bits (4 bytes), useful for sequence and
         * acknowledgement numbers.
         */
        public static byte[] intToFour(int number) {
                byte[] bytes = new byte[4];
                bytes[0] = (byte) (number >>> 24);
                bytes[1] = (byte) (number >>> 16);
                bytes[2] = (byte) (number >>> 8);
                bytes[3] = (byte) number;
                return bytes;
        }
}
