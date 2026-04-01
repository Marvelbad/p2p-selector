package com.cibit.p2p.selector.controller;

import com.cibit.p2p.selector.controller.api.P2pApi;
import com.cibit.p2p.selector.dto.request.CancelRequest;
import com.cibit.p2p.selector.dto.request.SaleRequest;
import com.cibit.p2p.selector.dto.request.StatusRequest;
import com.cibit.p2p.selector.dto.response.CancelResponse;
import com.cibit.p2p.selector.dto.response.SaleResponse;
import com.cibit.p2p.selector.dto.response.StatusResponse;
import com.cibit.p2p.selector.service.P2pService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class P2pController implements P2pApi {

    private final P2pService p2pService;

    @Override
    public SaleResponse sale(@Valid @RequestBody SaleRequest request) {
        log.info("[SALE] merchant={} order={} type={} amount={}",
                request.getMerchant_id(), request.getOrder(),
                request.getPayment_type(), request.getAmount());
        return p2pService.sale(request);
    }

    @Override
    public CancelResponse cancel(@Valid @RequestBody CancelRequest request) {
        log.info("[CANCEL] merchant={} invoice_id={}",
                request.getMerchant_id(), request.getInvoice_id());
        return p2pService.cancel(request);
    }

    @Override
    public StatusResponse status(@Valid @ModelAttribute StatusRequest request) {
        log.info("[STATUS] merchant={} order={}",
                request.getMerchant_id(), request.getOrder());
        return p2pService.status(request);
    }

    @Override
    public String health() {
        log.info("[HEALTH] сервис доступен");
        return "OK";
    }

}