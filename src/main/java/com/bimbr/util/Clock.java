package com.bimbr.util;

import java.util.Date;

/**
 * Class that provides system time. Used to allow incjecting preset time in unit tests to code that
 * relies on system time.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public class Clock {
    /**
     * @return current system time
     */
    public Date getTime() {
        return new Date();
    }
}
