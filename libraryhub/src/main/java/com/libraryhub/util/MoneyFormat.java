package com.libraryhub.util;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.stereotype.Component;

/** Formats amounts as rupees with two decimals. Exposed to templates as the bean "money". */
@Component("money")
public class MoneyFormat {

    private static final String RUPEE = "\u20B9";

    public String format(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return RUPEE + String.format(Locale.ENGLISH, "%,.2f", value);
    }
}
