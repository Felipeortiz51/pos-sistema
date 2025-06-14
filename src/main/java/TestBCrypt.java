import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        String contrasenaPlana = "admin";

        // 1. Creamos una instancia del codificador de Spring
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 2. Generamos el nuevo hash
        String hashGenerado = passwordEncoder.encode(contrasenaPlana);

        System.out.println("--- Generador de Hash con Spring Security ---");
        System.out.println("El nuevo hash para la contraseña '" + contrasenaPlana + "' es:");
        System.out.println(hashGenerado);
        System.out.println("--- Copia la línea de arriba y pégala en tu archivo SQL ---");
    }
}