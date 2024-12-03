package battleship.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.google.common.net.InetAddresses;

/**
 * Netzwerk-Werkzeuge
 */
public final class NetworkUtils {
    /**
     * NetworkUtils kann nicht instanziiert werden
     */
    private NetworkUtils() {
        throw new UnsupportedOperationException("NetworkUtils cannot be instantiated");
    }

    /**
     * Gibt eine Liste von IP-Adressen des lokalen Computers zurück.
     * Loopback-Adressen werden dabei ausgeschlossen. Die IP-Adressen werden soweit
     * gekürzt bzw. normalisiert soweit es Java zulässt.
     *
     * @return
     * @throws SocketException
     */
    public static List<String> getIpAddresses() throws SocketException {
        final List<String> ipAddresses = new ArrayList<>();

        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces != null) {
            for (final NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    for (final InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                        if (!address.isLoopbackAddress()) {
                            String ipAddress = InetAddresses.toAddrString(address);
                            // Entferne das `%` mit dem Interface bei nicht link lokalen Adressen
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
