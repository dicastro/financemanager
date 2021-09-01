package com.diegocastroviadero.financemanager.app.views.imports;

import com.diegocastroviadero.financemanager.app.model.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class SelectedAccount {
    private Account account;
}
