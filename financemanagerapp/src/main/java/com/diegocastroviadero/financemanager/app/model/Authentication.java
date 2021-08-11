package com.diegocastroviadero.financemanager.app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class Authentication {
    private String password;
}
