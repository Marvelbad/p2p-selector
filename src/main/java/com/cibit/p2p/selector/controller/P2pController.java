package com.cibit.p2p.selector.controller;

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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class P2pController {

    private final P2pService p2pService;

    @PostMapping("/p2p-selector/sale")
    public SaleResponse sale(@Valid @RequestBody SaleRequest request) {
        return p2pService.sale(request);
    }

    @PostMapping("/p2p-selector/cancel")
    public CancelResponse cancel(@Valid @RequestBody CancelRequest request) {
        return p2pService.cancel(request);
    }

    @GetMapping("/status")
    public StatusResponse status(@Valid @ModelAttribute StatusRequest request) {
        return p2pService.status(request);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

}