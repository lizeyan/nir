package cutter;

import java.util.List;

public interface Cutter {
    default List<String> cut(String x) {
        return null;
    }
}
