package com.master.domain_invoice_service.controller;

import com.master.domain_invoice_service.dto.InvoiceUserDto;
import com.master.domain_invoice_service.dto.validations.MandatoryFieldsValidation;
import com.master.domain_invoice_service.mapper.InvoiceMapper;
import com.master.domain_invoice_service.service.InvoiceService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/invoices/v1")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<InvoiceUserDto>> findAll() {
        var invoiceList = invoiceService.findAll().stream()
            .map(InvoiceMapper.INSTANCE::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceList);
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello world");
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceUserDto> findById(
        @PathVariable @Min(value = 1, message = "invalid Id") Long id) {
        var invoice = InvoiceMapper.INSTANCE.toDto(invoiceService.findInvoiceAggregatorById(id));
        return ResponseEntity.ok(invoice);
    }

    @PostMapping
    public ResponseEntity<Void> save(
        @RequestBody @Validated(MandatoryFieldsValidation.class) InvoiceUserDto invoiceUserDto) {
        var invoice = invoiceService.save(invoiceUserDto);
        var location = UriComponentsBuilder
            .fromPath("http://localhost:9090/api/invoices")
            .path("/v1/{id}")
            .buildAndExpand(invoice.getId())
            .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
        @PathVariable @Min(value = 1, message = "invalid Id") Long id,
        @RequestBody @Validated(MandatoryFieldsValidation.class) InvoiceUserDto invoiceUserDto) {
        invoiceService.update(id, invoiceUserDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable @Min(value = 1, message = "invalid Id") Long id) {
        invoiceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
