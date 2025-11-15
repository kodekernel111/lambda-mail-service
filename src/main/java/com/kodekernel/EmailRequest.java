package com.kodekernel;

import lombok.Data;

@Data
public class EmailRequest {
    private String name;
    private String email;
    private String message;
    private String service;
}