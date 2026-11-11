package com.example.visitsubsystem.web.api;

import com.example.visitsubsystem.domain.Hcp;
import com.example.visitsubsystem.domain.Product;

public record HcpResponse(long id, String name, long hospitalId, String hospitalName) {

    public static HcpResponse from(Hcp hcp) {
        return new HcpResponse(hcp.getId(), hcp.getName(), hcp.getHospital().getId(), hcp.getHospital().getName());
    }
}
