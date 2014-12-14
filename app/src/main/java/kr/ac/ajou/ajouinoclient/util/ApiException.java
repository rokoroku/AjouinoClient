package kr.ac.ajou.ajouinoclient.util;

/**
 * Exception class for ApiCaller
 */
public class ApiException extends Exception {

    public static final int ERROR_UNAUTHORIZED = 401;
    public static final int ERROR_DATA_NOT_FOUND = 404;
    public static final int ERROR_BAD_REQUEST = 400;
    public static final int ERROR_INTERNAL_SERVER_ERROR = 500;
    public static final int ERROR_SERVICE_UNAVAILABLE = 503;

    private final int errorCode;
    private final String errorMessage;

    public ApiException(int code) {
        super(getError(code));
        this.errorMessage = getError(code);
        this.errorCode = code;
    }

    public ApiException(int code, String message) {
        super(getError(code));
        this.errorMessage = message;
        this.errorCode = code;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public static String getError(int code) {
        switch (code) {
            case ERROR_UNAUTHORIZED:
                return "Unauthorized";
            case ERROR_BAD_REQUEST:
                return "Bad request";
            case ERROR_INTERNAL_SERVER_ERROR:
                return "Internal server error";
            case ERROR_DATA_NOT_FOUND:
                return "Data not found";
            case ERROR_SERVICE_UNAVAILABLE:
                return "Service unavailable";
        }
        return null;
    }
}