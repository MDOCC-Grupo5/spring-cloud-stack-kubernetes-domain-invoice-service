package com.master.domain_invoice_service.mapper;

import com.master.domain_invoice_service.dto.InvoiceAggregatorDto;
import com.master.domain_invoice_service.dto.InvoiceUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvoiceMapper {

    InvoiceMapper INSTANCE = Mappers.getMapper(InvoiceMapper.class);

    @Mapping(source = "invoice.id", target = "id")
    @Mapping(source = "invoice.createdAt", target = "createdAt")
    @Mapping(source = "invoice.totalAmount", target = "totalAmount")
    InvoiceUserDto toDto(InvoiceAggregatorDto invoiceAggregatorDto);

}
