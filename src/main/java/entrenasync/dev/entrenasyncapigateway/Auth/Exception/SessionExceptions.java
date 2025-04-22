package entrenasync.dev.entrenasyncapigateway.Auth.Exception;

public class SessionExceptions extends RuntimeException {
    public SessionExceptions(String message) {
        super(message);
    }

    public static class loginBadCredentialsException extends SessionExceptions {
        public loginBadCredentialsException(String data1, String data2) {super("Login Bad Credentials with " + data1 + "," + data2);}
    }
}
