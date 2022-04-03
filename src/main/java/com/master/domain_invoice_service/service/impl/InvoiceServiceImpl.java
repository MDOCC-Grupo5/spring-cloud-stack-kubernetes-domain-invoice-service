package com.master.domain_invoice_service.service.impl;

import com.master.domain_invoice_service.client.UserFeignClient;
import com.master.domain_invoice_service.dto.InvoiceAggregatorDto;
import com.master.domain_invoice_service.dto.InvoiceUserDto;
import com.master.domain_invoice_service.entity.Invoice;
import com.master.domain_invoice_service.exception.ObjectNotFoundException;
import com.master.domain_invoice_service.repository.InvoiceRepository;
import com.master.domain_invoice_service.service.InvoiceService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceAggregatorDto> findAll() {
        return invoiceRepository.findAll().stream()
            .map(this::toInvoiceAggregatorDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
            .orElseThrow(
                () -> new ObjectNotFoundException("Invoice with id '" + id + "', not found"));
    }

    @Override
    @Transactional
    public InvoiceAggregatorDto findInvoiceAggregatorById(Long id) {
        return toInvoiceAggregatorDto(findById(id));
    }

    @Override
    @Transactional
    public Invoice save(InvoiceUserDto invoiceUserDto) {
        var invoice = Invoice.builder()
            .userId(invoiceUserDto.getUser().getId())
            .createdAt(LocalDateTime.now())
            .totalAmount(invoiceUserDto.getTotalAmount())
            .build();
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void update(Long id, InvoiceUserDto invoiceUserDto) {
        var invoice = findById(id).toBuilder()
            .userId(invoiceUserDto.getUser().getId())
            .totalAmount(invoiceUserDto.getTotalAmount())
            .build();
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        var invoice = findById(id);
        invoiceRepository.delete(invoice);
    }

    private InvoiceAggregatorDto toInvoiceAggregatorDto(Invoice invoice) {
        var userDto = userFeignClient.findById(invoice.getUserId());
        return InvoiceAggregatorDto.builder()
            .invoice(invoice)
            .user(userDto)
            .build();
    }

}
