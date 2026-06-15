package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

/**
 * SafeDbCall
 *  Wrapper for dbcalls and exception handling.
 * @author Peter Rutschmann
 */
public class SafeDbCall {
    private static final Logger log = LoggerFactory.getLogger(SafeDbCall.class);

    //wrapper for dbcalls and exception handling for suppliers
    public static <T> T safeDbCall(Supplier<T> dbCall, T fallback) {
        try {
            return dbCall.get();
        } catch (DataAccessException ex) {
            log.warn("db access failed: ", ex);
            return fallback;
        }
    }
    //wrapper for db-calls and exception handling for runables
    public static boolean safeDbCall(Runnable dbAction) {
        try {
            dbAction.run();
            return true;
        } catch (DataAccessException ex) {
            log.warn("db access failed: ", ex);
            return false;
        }
    }
}