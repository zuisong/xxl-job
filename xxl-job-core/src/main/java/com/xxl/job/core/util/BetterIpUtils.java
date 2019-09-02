package com.xxl.job.core.util;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class BetterIpUtils {

    private static volatile String localIp = null;

    public static String getLocalIp() {

        if (localIp != null) {
            return localIp;
        }

        try {
            Inet4Address inet4Address = getLocalIp4Address();
            if (inet4Address != null) {
                localIp = inet4Address.getHostAddress();
                return localIp;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Inet4Address getIpBySocket() throws SocketException {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            if (socket.getLocalAddress() instanceof Inet4Address) {
                return (Inet4Address) socket.getLocalAddress();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static Inet4Address getLocalIp4Address() throws SocketException {
        final List<Inet4Address> ipByNi = getLocalIp4AddressFromNetworkInterface();
        if (ipByNi.size() != 1) {
            Inet4Address ipBySocketOpt = getIpBySocket();
            if (ipBySocketOpt != null) {
                return ipBySocketOpt;
            }
        }
        return ipByNi.isEmpty() ? null : ipByNi.get(0);
    }

    private static List<Inet4Address> getLocalIp4AddressFromNetworkInterface() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(1);
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        if (e == null) {
            return addresses;
        }
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            if (!isValidInterface(n)) {
                continue;
            }
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                if (isValidAddress(i)) {
                    addresses.add((Inet4Address) i);
                }
            }
        }
        return addresses;
    }

    /**
     * 过滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
     *
     * @param ni 网卡
     * @return 如果满足要求则true，否则false
     */
    private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
    }

    /**
     * 判断是否是IPv4，并且内网地址并过滤回环地址.
     */
    private static boolean isValidAddress(InetAddress address) {
        return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
    }

}
