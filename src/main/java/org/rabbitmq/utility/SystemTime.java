package org.rabbitmq.utility;

import java.util.Calendar;

import javax.annotation.concurrent.NotThreadSafe;

import org.joda.time.DateTime;

@NotThreadSafe
public final class SystemTime {

    private static final int MILLIS_IN_SEC_1000 = 1000;

    private SystemTime() {}

    private static final TimeSource DEFAULT_SRC = new TimeSource() {
        @Override
        public long millis() {
            return System.currentTimeMillis();
        }
    };

    private static TimeSource source = null;

    public static void reset() {
        setTimeSource(null);
    }

    public static long asMillis() {
        return getTimeSource().millis();
    }

    public static int asSeconds() {
        return (int) (getTimeSource().millis() / MILLIS_IN_SEC_1000);
    }

    public static DateTime asDate() {
        return new DateTime(getTimeSource().millis());
    }

    public static Calendar asCalendar() {
        return asDate().toGregorianCalendar();
    }

    public static void setTimeSource(TimeSource timeSource) {
        source = timeSource;
    }

    private static TimeSource getTimeSource() {
        return source == null ? DEFAULT_SRC : source;
    }

}
