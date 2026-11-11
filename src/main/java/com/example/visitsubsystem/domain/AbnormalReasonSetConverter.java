package com.example.visitsubsystem.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;

@Converter
public class AbnormalReasonSetConverter implements AttributeConverter<Set<AbnormalReason>, String> {

    @Override
    public String convertToDatabaseColumn(Set<AbnormalReason> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(",");
        EnumSet.copyOf(attribute).forEach(reason -> joiner.add(reason.name()));
        return joiner.toString();
    }

    @Override
    public Set<AbnormalReason> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptySet();
        }
        EnumSet<AbnormalReason> reasons = EnumSet.noneOf(AbnormalReason.class);
        for (String token : dbData.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                reasons.add(AbnormalReason.valueOf(trimmed));
            } catch (IllegalArgumentException ignored) {
                // Unknown codes from future versions are skipped rather than failing the read.
            }
        }
        return reasons;
    }
}
