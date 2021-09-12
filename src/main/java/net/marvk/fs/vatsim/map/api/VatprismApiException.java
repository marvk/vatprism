package net.marvk.fs.vatsim.map.api;

import java.io.Serial;

public class VatprismApiException extends Exception {
    @Serial
    private static final long serialVersionUID = 6838213101575454645L;

    public VatprismApiException() {
    }

    public VatprismApiException(final String message) {
        super(message);
    }

    public VatprismApiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public VatprismApiException(final Throwable cause) {
        super(cause);
    }

    public VatprismApiException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
