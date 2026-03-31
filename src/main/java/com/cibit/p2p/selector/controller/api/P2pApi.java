package com.cibit.p2p.selector.controller.api;

import com.cibit.p2p.selector.dto.request.CancelRequest;
import com.cibit.p2p.selector.dto.request.SaleRequest;
import com.cibit.p2p.selector.dto.request.StatusRequest;
import com.cibit.p2p.selector.dto.response.CancelResponse;
import com.cibit.p2p.selector.dto.response.SaleResponse;
import com.cibit.p2p.selector.dto.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "P2P Selector API",
        description = "API для обработки P2P-переводов. Поддерживает переводы на карту (card), по СБП (sbp) и по номеру счёта (bank_transfer)."
)
public interface P2pApi {

    @Operation(
            summary = "Создание платежа",
            description = """
                    Создаёт P2P-платёж и возвращает реквизиты получателя.
                    
                    Типы платежа:
                    - card — возвращает PAN карты получателя
                    - sbp — возвращает номер телефона для СБП перевода
                    - bank_transfer — возвращает номер счёта получателя
                    
                    Важно: если платёж не будет произведён — обязательно отменить через /cancel,
                    иначе упрётесь в лимиты.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Платёж создан успешно",
                    content = @Content(schema = @Schema(implementation = SaleResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверная подпись запроса"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/p2p-selector/sale")
    SaleResponse sale(@Valid @RequestBody SaleRequest request);

    @Operation(
            summary = "Отмена платежа",
            description = "Отменяет ранее созданный платёж по invoice_id. Обязательно вызывать если платёж не был произведён."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Платёж успешно отменён",
                    content = @Content(schema = @Schema(implementation = CancelResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверная подпись запроса"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей"),
            @ApiResponse(responseCode = "404", description = "Платёж не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/p2p-selector/cancel")
    CancelResponse cancel(@Valid @RequestBody CancelRequest request);

    @Operation(
            summary = "Статус платежа",
            description = """
                    Возвращает текущий статус платежа по номеру заказа.
                    
                    Статусы: new, pending, complete, partial_complete, failed.
                    При статусе failed дополнительно возвращаются last_payment_error_code и last_payment_error.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус получен",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверная подпись запроса"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей"),
            @ApiResponse(responseCode = "404", description = "Платёж не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/status")
    StatusResponse status(@Valid @ModelAttribute StatusRequest request);

    @Operation(summary = "Проверка доступности сервиса", description = "Возвращает OK если сервис запущен.")
    @ApiResponse(responseCode = "200", description = "Сервис работает нормально")
    @GetMapping("/health")
    String health();
}
