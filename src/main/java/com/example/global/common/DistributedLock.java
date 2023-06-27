package com.example.global.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * name
     */
    String key();

    /**
     * Time Unit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * lock wait time (default - 5 Sec)
     * get lock
     */
    long waitTime() default 5L;

    /**
     * lock leaseTime (default - 3 Sec)
     * clear lock
     */
    long leaseTime() default 3L;
}
