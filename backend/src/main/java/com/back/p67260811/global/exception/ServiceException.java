package com.back.p67260811.global.exception;

import com.back.p67260811.global.dto.RsData;
import lombok.Getter;

public class ServiceException extends RuntimeException {

    private final RsData<?> rsData;

    public ServiceException(String resultCode, String msg) {
        super(msg);
        this.rsData = new RsData<>(
                resultCode,
                msg
        );
    }

    public RsData<?> getRsData() { return rsData; }

    public String getResultCode() {
        return rsData.getResultCode();
    }

    public String getMsg() {
        return rsData.getMsg();
    }

}
