package com.master.domain_invoice_service.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto implements Serializable {

    private static final long serialVersionUID = 552992342L;

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

}
