package com.back.p67260811.global.exception

import com.back.p67260811.global.dto.RsData

class ServiceException(
    @JvmField val resultCode: String,
    @JvmField val msg: String
) :
    RuntimeException("$resultCode : $msg") {
    val rsData: RsData<Void>
        get() = RsData(resultCode, msg)
}
