package org.hadatac.entity.pojo;

public class Pair<K, V> {

    private final K elementL;
    private final V elementR;

    public static <K, V> Pair<K, V> createPair(K elementL, V elementR) {
        return new Pair<K, V>(elementL, elementR);
    }

    public Pair(K elementL, V elementR) {
        this.elementL = elementL;
        this.elementR = elementR;
    }

    public K getLeft() {
        return elementL;
    }

    public V getRight() {
        return elementR;
    }

}