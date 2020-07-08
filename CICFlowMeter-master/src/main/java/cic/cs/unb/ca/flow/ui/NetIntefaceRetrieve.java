package com.company;

import java.net.*;
import java.util.Enumeration;

public class Main {

    public static void main(String[] args) throws SocketException, UnknownHostException {
        final Enumeration<NetworkInterface> netifs = NetworkInterface.getNetworkInterfaces();
        String ip;
        //InetAddress myAddr = InetAddress.getLocalHost();
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
             ip = socket.getLocalAddress().getHostAddress();
            System.out.println(ip);
        }
        InetAddress myAddr = InetAddress.getByName(ip);
        while (netifs.hasMoreElements()) {
            NetworkInterface networkInterface = netifs.nextElement();
            Enumeration<InetAddress> inAddrs = networkInterface.getInetAddresses();
            while (inAddrs.hasMoreElements()) {
                InetAddress inAddr = inAddrs.nextElement();
                if (inAddr.equals(myAddr)) {
                    System.out.println(networkInterface.getName());
                    System.out.println(networkInterface.getDisplayName());

                }


            }
        }
    }

}
