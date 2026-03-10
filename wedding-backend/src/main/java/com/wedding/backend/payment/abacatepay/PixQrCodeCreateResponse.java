package com.wedding.backend.payment.abacatepay;

public class PixQrCodeCreateResponse {

    private PixQrCode data;
    private Object error;

    public PixQrCode getData() {
        return data;
    }

    public void setData(PixQrCode data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public static class PixQrCode {
        private String id;
        private long amount;
        private String status;
        private boolean devMode;
        private String brCode;
        private String brCodeBase64;
        private long platformFee;
        private String createdAt;
        private String updatedAt;
        private String expiresAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isDevMode() {
            return devMode;
        }

        public void setDevMode(boolean devMode) {
            this.devMode = devMode;
        }

        public String getBrCode() {
            return brCode;
        }

        public void setBrCode(String brCode) {
            this.brCode = brCode;
        }

        public String getBrCodeBase64() {
            return brCodeBase64;
        }

        public void setBrCodeBase64(String brCodeBase64) {
            this.brCodeBase64 = brCodeBase64;
        }

        public long getPlatformFee() {
            return platformFee;
        }

        public void setPlatformFee(long platformFee) {
            this.platformFee = platformFee;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}

