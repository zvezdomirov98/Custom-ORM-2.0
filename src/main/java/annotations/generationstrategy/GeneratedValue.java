package annotations.generationstrategy;

import annotations.generationstrategy.GenerationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {
    GenerationType strategy();
}
