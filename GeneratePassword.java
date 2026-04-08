
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin1234";
        String hashedPassword = encoder.encode(password);
        System.out.println(hashedPassword);
    }
}
