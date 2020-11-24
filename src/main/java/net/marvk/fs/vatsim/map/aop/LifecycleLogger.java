package net.marvk.fs.vatsim.map.aop;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class LifecycleLogger implements MethodInterceptor {

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        final String className = methodInvocation.getThis().getClass().getSimpleName().replaceFirst("\\$\\$.*$", "");

        log.info("Reload start: " + className);
        try {
            final Object proceed = methodInvocation.proceed();
            log.info("Reload completed: " + className);
            return proceed;
        } catch (final Throwable t) {
            log.error("Reload failed: " + className);
            throw t;
        }
    }
}
