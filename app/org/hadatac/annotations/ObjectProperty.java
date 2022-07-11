package org.hadatac.annotations;

public @interface ObjectProperty {
    int cardinality() default 1;  // 1 - one element; 2 - many elements
    String objectTypeUri();
}
