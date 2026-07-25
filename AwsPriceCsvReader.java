package awsim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small RFC-4180-style reader for AWS Price List CSV streams.
 *
 * <p>AWS price files contain five metadata records followed by a service-specific
 * header. The implementation is dependency-free, supports quoted commas,
 * escaped quotes, and embedded newlines, and processes records incrementally so
 * the large EC2 offer file is never held in memory.
 */
final class AwsPriceCsvReader {
    interface RowHandler {
        void accept(Row row) throws IOException;
    }

    static final class OfferMetadata {
        private final Map<String, String> values;

        OfferMetadata(Map<String, String> values) {
            this.values = Collections.unmodifiableMap(new HashMap<>(values));
        }

        String get(String key) {
            return values.get(key);
        }
    }

    static final class Row {
        private final Map<String, Integer> columns;
        private final List<String> values;

        Row(Map<String, Integer> columns, List<String> values) {
            this.columns = columns;
            this.values = values;
        }

        String get(String name) {
            Integer index = columns.get(name);
            if (index == null || index < 0 || index >= values.size()) {
                return "";
            }
            return values.get(index);
        }

        boolean is(String name, String expected) {
            return expected.equalsIgnoreCase(get(name));
        }

        double price() {
            String value = get("PricePerUnit");
            if (value.isEmpty()) {
                return Double.NaN;
            }
            return Double.parseDouble(value);
        }
    }

    private AwsPriceCsvReader() {
    }

    static OfferMetadata read(Reader source, RowHandler handler) throws IOException {
        BufferedReader reader = source instanceof BufferedReader
                ? (BufferedReader) source
                : new BufferedReader(source, 64 * 1024);
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            List<String> record = readRecord(reader);
            if (record == null || record.size() < 2) {
                throw new IOException("Malformed AWS Price List metadata record " + (i + 1));
            }
            metadata.put(record.get(0), record.get(1));
        }

        List<String> header = readRecord(reader);
        if (header == null || header.isEmpty()) {
            throw new IOException("AWS Price List CSV has no header");
        }
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            columns.put(header.get(i), i);
        }
        for (String required : new String[]{"TermType", "PricePerUnit", "Unit"}) {
            if (!columns.containsKey(required)) {
                throw new IOException("AWS Price List CSV is missing column " + required);
            }
        }

        List<String> record;
        while ((record = readRecord(reader)) != null) {
            if (!record.isEmpty()) {
                handler.accept(new Row(columns, record));
            }
        }
        return new OfferMetadata(metadata);
    }

    private static List<String> readRecord(BufferedReader reader) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean sawAny = false;

        while (true) {
            int next = reader.read();
            if (next < 0) {
                if (!sawAny && field.length() == 0 && fields.isEmpty()) {
                    return null;
                }
                fields.add(field.toString());
                return fields;
            }
            sawAny = true;
            char ch = (char) next;

            if (quoted) {
                if (ch == '"') {
                    reader.mark(1);
                    int after = reader.read();
                    if (after == '"') {
                        field.append('"');
                    } else {
                        quoted = false;
                        if (after >= 0) {
                            reader.reset();
                        }
                    }
                } else {
                    field.append(ch);
                }
                continue;
            }

            if (ch == '"' && field.length() == 0) {
                quoted = true;
            } else if (ch == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (ch == '\n') {
                fields.add(field.toString());
                return fields;
            } else if (ch == '\r') {
                reader.mark(1);
                int after = reader.read();
                if (after != '\n' && after >= 0) {
                    reader.reset();
                }
                fields.add(field.toString());
                return fields;
            } else {
                field.append(ch);
            }
        }
    }
}
