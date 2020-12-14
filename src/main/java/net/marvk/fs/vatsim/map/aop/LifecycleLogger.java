package net.marvk.fs.vatsim.map.aop;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class LifecycleLogger implements MethodInterceptor {
    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        final String className = methodInvocation.getThis().getClass().getSimpleName().replaceFirst("\\$\\$.*$", "");
        final String methodName = methodInvocation.getMethod().getName();

        final LogLifecycle logLifecycle = methodInvocation.getMethod().getAnnotation(LogLifecycle.class);

        final String identifier = className + "." + methodName;

        final LocalDateTime start = LocalDateTime.now();
        if (logLifecycle.logStart()) {
            log.info("Starting " + identifier);
        }
        try {
            final Object result = methodInvocation.proceed();
            if (logLifecycle.logCompleted()) {
                log.info("Completed " + identifier + " in " + duration(start));
            }
            return result;
        } catch (final Throwable t) {
            if (logLifecycle.logFailed()) {
                log.error("Failed " + identifier + " in " + duration(start));
            }
            throw t;
        }
    }

    private static Duration duration(final LocalDateTime start) {
        return Duration.between(start, LocalDateTime.now());
    }
}
