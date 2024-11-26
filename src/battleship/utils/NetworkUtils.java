package battleship.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class NetworkUtils {
    private NetworkUtils() {
        throw new UnsupportedOperationException("NetworkUtils cannot be instantiated");
    }

    public static List<String> getIpAddresses() throws SocketException {
        final ArrayList<String> ipAddresses = new ArrayList<>();

        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces != null) {
            for (final NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    for (final InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                        if (!address.isLoopbackAddress()) {
                            String ipAddress = address.getHostAddress();
                            if (
                                address instanceof java.net.Inet6Address && !address.isLinkLocalAddress()
                                        && ipAddress.contains("%")
                            ) {
                                ipAddress = ipAddress.substring(0, ipAddress.indexOf('%'));
                            }
                            ipAddresses.add(ipAddress);
                        }
                    }
                }
            }
        }

        return ipAddresses;
    }
}
