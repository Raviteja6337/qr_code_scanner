package com.example.qrcodescanner.ui;

import java.io.Serializable;

public class QRDetailsDAO implements Serializable {
    private String strQRData;
    private String timeStampVal;

    public String getStrQRData() {
        return strQRData;
    }

    public void setStrQRData(String strQRData) {
        this.strQRData = strQRData;
    }

    public String getTimeStampVal() {
        return timeStampVal;
    }

    public void setTimeStampVal(String timeStampVal) {
        this.timeStampVal = timeStampVal;
    }
}
