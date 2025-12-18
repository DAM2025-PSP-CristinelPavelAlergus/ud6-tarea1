import static java.lang.IO.println;

/**
 * Amplía el programa anterior (Ejercicio 5) para mostrar información de la configuración IP para cada interfaz, al menos la dirección o direcciones IP, y las correspondientes máscaras de red y
 * direcciones de broadcast. Compara la información mostrada por el programa con la mostrada al ejecutar el comando ipconfig o ifconfig.
 */
void main() {
    try {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        if (!networkInterfaces.hasMoreElements()) {
            println("Este ordenador no tiene interfaces de red.");
            return;
        }

        println("--- Interfaces de red ---\n");

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (isNotPhysicalInterface(networkInterface)) {
                continue; // Ignoramos las interfaces virtuales
            }
            println("Nombre: " + networkInterface.getDisplayName());
            println("Active: " + networkInterface.isUp());
            println("Tiene conexión a internet: " + hasInternetAccess(networkInterface));
            printIps(networkInterface);
            println();
            println("--------------------------\n");
        }
    } catch (SocketException e) {
        System.err.println("No se pudo obtener la lista de interfaces de red: " + e.getLocalizedMessage());
    }
}

static boolean isNotPhysicalInterface(NetworkInterface networkInterface) throws SocketException {
    return networkInterface.isLoopback()
           || networkInterface.isVirtual()
           || networkInterface.getHardwareAddress() == null;
}

static boolean hasInternetAccess(NetworkInterface netIf) {
    Enumeration<InetAddress> addresses = netIf.getInetAddresses();

    while (addresses.hasMoreElements()) {
        InetAddress localAddr = addresses.nextElement();

        if (localAddr instanceof Inet6Address) {
            continue;
        }

        try (Socket socket = new Socket()) {
            socket.bind(new InetSocketAddress(localAddr, 0));
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000);

            return true; // Si conecta, esta interfaz tiene salida
        } catch (IOException _) {
        }
    }

    return false; // Si ninguna IP de la interfaz logró conectar
}

private static void printIps(NetworkInterface networkInterface) {
    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
        InetAddress address = interfaceAddress.getAddress();

        if (address instanceof Inet4Address) {
            println("IPv4: " + address.getHostAddress());

            short prefixLength = interfaceAddress.getNetworkPrefixLength();
            println("\tMáscara: " + getSubnetMask(prefixLength) + " (/" + prefixLength + ")");

            if (interfaceAddress.getBroadcast() != null) {
                println("\tBroadcast: " + interfaceAddress.getBroadcast().getHostAddress());
            }
        } else if (address instanceof Inet6Address) {
            println("IPv6: " + address.getHostAddress() + "/" + interfaceAddress.getNetworkPrefixLength());
        }
    }
}

private static String getSubnetMask(short prefixLength) {
    int mask = 0xffffffff << (32 - prefixLength);
    return String.format("%d.%d.%d.%d",
            (mask >> 24) & 0xff,
            (mask >> 16) & 0xff,
            (mask >> 8) & 0xff,
            mask & 0xff);
}