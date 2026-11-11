package com.example.visitsubsystem.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AbnormalReasonSetConverterTest {

    private final AbnormalReasonSetConverter converter = new AbnormalReasonSetConverter();

    @Test
    void writesCodesInStableEnumOrder() {
        String dbData = converter.convertToDatabaseColumn(
                EnumSet.of(AbnormalReason.FAR_FROM_HOSPITAL, AbnormalReason.SHORT_DURATION));

        assertEquals("SHORT_DURATION,FAR_FROM_HOSPITAL", dbData);
    }

    @Test
    void emptyOrNullSetIsPersistedAsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToDatabaseColumn(Set.of()));
    }

    @Test
    void readsCodesIncludingWhitespace() {
        assertEquals(EnumSet.of(AbnormalReason.SHORT_DURATION),
                converter.convertToEntityAttribute(" SHORT_DURATION "));
    }

    @Test
    void skipsUnknownCodesButKeepsKnownOnes() {
        assertEquals(EnumSet.of(AbnormalReason.FAR_FROM_HOSPITAL),
                converter.convertToEntityAttribute("FAR_FROM_HOSPITAL,NOT_A_REASON"));
    }

    @Test
    void blankColumnReadsAsEmptySet() {
        assertEquals(EnumSet.noneOf(AbnormalReason.class), converter.convertToEntityAttribute(null));
        assertEquals(EnumSet.noneOf(AbnormalReason.class), converter.convertToEntityAttribute("  "));
    }
}
