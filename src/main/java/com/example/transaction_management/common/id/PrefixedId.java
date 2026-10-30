package com.example.transaction_management.common.id;

import org.hibernate.annotations.IdGeneratorType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(PrefixedIdGenerator.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface PrefixedId {
    String prefix();
}