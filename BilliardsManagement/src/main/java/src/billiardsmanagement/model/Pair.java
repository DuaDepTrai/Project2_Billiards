package src.billiardsmanagement.model;

import java.util.List;

public class Pair<T, U> {
    private T t;
    private U u;

    public Pair(T t, U u) {
        this.t = t;
        this.u = u;
    }

    public Pair() {

    }

    public T getFirstValue() {
        return t;
    }

    public U getSecondValue() {
        return u;
    }

    public Pair<T, U> setFirstValue(T first) {
        t = first;
        return this;
    }

    public Pair<T, U> setSecondValue(U second) {
        u = second;
        return this;
    }

    // New method to get second value by first value
    public U getSecondByFirstValue(T firstValue) {
        return firstValue.equals(t) ? u : null;
    }

    // New method to get first value by second value
    public T getFirstBySecondValue(U secondValue) {
        return secondValue.equals(u) ? t : null;
    }

    // Static method to find first value by second value in a list of pairs
    public static <T, U> T getFirstBySecondValue(List<Pair<T, U>> pairs, U secondValue) {
        for (Pair<T, U> pair : pairs) {
            T firstValue = pair.getFirstBySecondValue(secondValue);
            if (firstValue != null) {
                return firstValue;
            }
        }
        return null;
    }
}
