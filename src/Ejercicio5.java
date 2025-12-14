import static java.lang.IO.println;

/**
 * Escribe un programa que muestre información de todas las interfaces de red presentes físicas en el sistema. Solo muestra información de las interfaces de red. Antes de ejecutar el programa,
 * asegúrate de que el ordenador tenga conexión a internet, o al menos a una red local.
 */
void main() {
    try {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        if (!networkInterfaces.hasMoreElements()) {
            println("Este ordenador no tiene interfaces de red.");
            return;
        }

        println("--- Interfaces de red ---\n");

        System.out.printf("| %-10s | %-10s | %-10s |%n", "Nombre", "Activa", "Con conexión a internet");
        println("-".repeat(53));

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (isNotPhysicalInterface(networkInterface)) {
                continue; // Ignoramos las interfaces virtuales
            }
            System.out.printf("| %-10s | %-10b | %-23b |%n", networkInterface.getDisplayName(), networkInterface.isUp(), hasInternetAccess(networkInterface));
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