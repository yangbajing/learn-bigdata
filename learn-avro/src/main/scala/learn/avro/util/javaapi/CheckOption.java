package learn.avro.util.javaapi;

import org.apache.avro.Schema;

import java.util.Map;

public class CheckOption {
    private final Schema.Field f;
    private final Object datum;
    private final Map<String, Object> props;

    public CheckOption(Schema.Field f, Object datum) {
        this.f = f;
        this.datum = datum;
        this.props = f.getObjectProps();
    }

    public void check() {
        switch (f.schema().getType()) {
            case STRING:
                String v = (String) datum;
                checkMin(v);
                checkMax(v);
                break;
            case INT:

                break;
            case LONG:

                break;
            case FLOAT:

                break;
            case DOUBLE:

                break;
            default: // do nothing
        }
    }

    private void checkMin(String v) {
        Object obj = props.get("min");
        if (null != obj) {
            Integer min = (Integer) obj;
            require(v.length() >= min, "String length must be >= $min, received [${f.name()}] is '$v'.");
        }
    }

    private void checkMax(String v) {
        Object obj = props.get("max");
        if (null != obj) {
            Integer max = (Integer) obj;
            require(v.length() <= max, "String length must be <= $max, received [${f.name()}] is '$v'.");

        }
    }

    private <T extends Comparable<T>> void checkMin(T v) {
        Object obj = props.get("max");
        if (null != obj) {
            Comparable<T> min = (Comparable<T>) obj;
            require(min.compareTo(v) <= 0, "${min.getClass.getSimpleName} must be >= $min, received [${f.name()}] is $v.");
        }
    }

    private void require(Boolean requirement, String message) {
        if (!requirement)
            throw new IllegalArgumentException(message);
    }
}
