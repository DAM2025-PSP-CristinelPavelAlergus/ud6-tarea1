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

        System.out.printf("| %-10s | %-10s |%n", "Nombre", "Activa");
        println("-".repeat(27));

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (isNotPhysicalInterface(networkInterface)) {
                continue; // Ignoramos las interfaces virtuales
            }
            System.out.printf("| %-10s | %-10b |%n",
                    networkInterface.getDisplayName(),
                    networkInterface.isUp());
        }
    } catch (SocketException e) {
        System.err.println("No se pudo obtener la lista de interfaces de red: " + e.getLocalizedMessage());
    }
}

static boolean isNotPhysicalInterface(NetworkInterface ni) throws SocketException {
    return ni.isLoopback()
           || ni.isVirtual()
           || ni.getHardwareAddress() == null;
}
