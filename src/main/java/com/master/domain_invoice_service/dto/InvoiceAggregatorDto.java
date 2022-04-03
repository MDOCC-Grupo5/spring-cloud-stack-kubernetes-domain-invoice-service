package com.master.domain_invoice_service.dto;

import com.master.domain_invoice_service.entity.Invoice;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceAggregatorDto implements Serializable {

    private static final long serialVersionUID = 1342342342L;

    private Invoice invoice;
    private UserDto user;

}
