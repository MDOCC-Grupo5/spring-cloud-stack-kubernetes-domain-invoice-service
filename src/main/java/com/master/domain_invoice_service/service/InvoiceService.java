package com.master.domain_invoice_service.service;

import com.master.domain_invoice_service.dto.InvoiceAggregatorDto;
import com.master.domain_invoice_service.dto.InvoiceUserDto;
import com.master.domain_invoice_service.entity.Invoice;
import java.util.List;

public interface InvoiceService {

    List<InvoiceAggregatorDto> findAll();

    Invoice findById(Long id);

    InvoiceAggregatorDto findInvoiceAggregatorById(Long id);

    Invoice save(InvoiceUserDto invoiceUserDto);

    void update(Long id, InvoiceUserDto invoiceUserDto);

    void deleteById(Long id);

}
