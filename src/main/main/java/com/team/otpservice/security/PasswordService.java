package main.main.java.com.team.otpservice.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {
    public String hash(String value) {
        return BCrypt.hashpw(value, BCrypt.gensalt());
    }

    public boolean matches(String raw, String hash) {
        return BCrypt.checkpw(raw, hash);
    }
}
