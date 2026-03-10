package com.wedding.backend.payment.abacatepay;

public class PixQrCodeStatusResponse {

    private StatusData data;
    private Object error;

    public StatusData getData() {
        return data;
    }

    public void setData(StatusData data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public static class StatusData {
        private String status;
        private String expiresAt;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}

