package net.cube.engine;

/**
 * @author pluto
 * @date 2022/5/16
 */
public class CubeRuntimeException extends RuntimeException {

    protected String errorCode;

    public CubeRuntimeException(String message) {
        super(message);
    }

    public CubeRuntimeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CubeRuntimeException(Throwable t) {
        super(t);
    }

    public CubeRuntimeException(String message, Throwable t) {
        super(message, t);
    }

    public CubeRuntimeException(String errorCode, String message, Throwable t) {
        super(message, t);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
