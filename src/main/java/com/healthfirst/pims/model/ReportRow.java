package com.healthfirst.pims.model;

/** Generic report row for the report table. Values are preformatted by the report query consumer. */
public record ReportRow(Object[] values) { }
