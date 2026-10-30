package com.example.transaction_management.common.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.UUID;

public class PrefixedIdGenerator implements BeforeExecutionGenerator {

    private final String prefix;

    public PrefixedIdGenerator(PrefixedId config, Member member, GeneratorCreationContext context) {
        this.prefix = config.prefix();
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner,
                           Object currentValue, EventType eventType) {
        return prefix + UUID.randomUUID();      // USER_3f9a1c7e-5b2d-4a60-9c1e-0a7b2d4e6f81
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}