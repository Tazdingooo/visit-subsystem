package com.example.visitsubsystem.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;

@Converter
public class AbnormalReasonSetConverter implements AttributeConverter<Set<AbnormalReason>, String> {

    private static final Logger log = LoggerFactory.getLogger(AbnormalReasonSetConverter.class);

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
            } catch (IllegalArgumentException ex) {
                // Kept for forward compatibility with codes added by newer deployments,
                // but surfaced in the log so a typo or rename does not go unnoticed.
                log.warn("Ignoring unknown abnormal reason code '{}' found in visit data", trimmed);
            }
        }
        return reasons;
    }
}
