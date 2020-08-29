package org.acme;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import io.quarkus.arc.ContextInstanceHandle;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.impl.ContextInstanceHandleImpl;

public class CallScopeContext implements InjectableContext { // InjectableContext extends AlterableContext extends Context

	static final ThreadLocal<Map<Contextual<?>, ContextInstanceHandle<?>>> ACTIVE_SCOPE_ON_THREAD = new ThreadLocal<>();

	@Override
	public Class<? extends Annotation> getScope() {
		return CallScoped.class;
	}

	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = ACTIVE_SCOPE_ON_THREAD.get();
		if (activeScope == null) {
			throw new ContextNotActiveException();
		}

		@SuppressWarnings("unchecked")
		ContextInstanceHandle<T> contextInstanceHandle = (ContextInstanceHandle<T>) activeScope.computeIfAbsent(
				contextual,
				c -> {
					if (creationalContext == null) {
						return null;
					}
					T createdInstance = contextual.create(creationalContext);
					return new ContextInstanceHandleImpl<T>((InjectableBean<T>) contextual, createdInstance,
							creationalContext);
				});

		return contextInstanceHandle.get();
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = ACTIVE_SCOPE_ON_THREAD.get();
		if (activeScope == null) {
			throw new ContextNotActiveException();
		}
		
		@SuppressWarnings("unchecked")
		ContextInstanceHandle<T> contextInstanceHandle = (ContextInstanceHandle<T>) activeScope.get(contextual);

		if (contextInstanceHandle == null) {
			return null;
		}

		return contextInstanceHandle.get();
	}

	@Override
	public boolean isActive() {
		return ACTIVE_SCOPE_ON_THREAD.get() != null;
	}

	@Override
	public void destroy(Contextual<?> contextual) {
		ContextInstanceHandle<?> contextInstanceHandle = ACTIVE_SCOPE_ON_THREAD.get().get(contextual);
		if (contextInstanceHandle != null) {
			contextInstanceHandle.destroy();
		}
	}

	/**
	 *  Two methods below are specific to Quarkus because defined on InjectableContext.
	 */
	
	@Override
	public void destroy() {
        Map<Contextual<?>, ContextInstanceHandle<?>> context = ACTIVE_SCOPE_ON_THREAD.get();
        if (context == null) {
            throw new ContextNotActiveException();
        }
        context.values().forEach(ContextInstanceHandle::destroy);
	}

	@Override
	public ContextState getState() {
		return new ContextState() {

            @Override
            public Map<InjectableBean<?>, Object> getContextualInstances() {
        		Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = ACTIVE_SCOPE_ON_THREAD.get();

                if (activeScope != null) {
                    return activeScope.values().stream()
                            .collect(Collectors.toMap(ContextInstanceHandle::getBean, ContextInstanceHandle::get));
                }
                return Collections.emptyMap();
            }
        };
	}
	
	/**
	 * Own API
	 */
	private final static CallScopeContext INSTANCE = new CallScopeContext();
	
	public static CallScopeContext get() {
		return INSTANCE;
	}
	
	public void enter() {
		Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = ACTIVE_SCOPE_ON_THREAD.get();

		if (activeScope != null) {
			throw new IllegalStateException("An instance of this scope is already active");
		}
		
		ACTIVE_SCOPE_ON_THREAD.set(new ConcurrentHashMap<>());
	}

	public void exit() {
		Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = ACTIVE_SCOPE_ON_THREAD.get();

		if (activeScope == null) {
			throw new IllegalStateException("Scope currently not active");
		}
		
		ACTIVE_SCOPE_ON_THREAD.set(null);
	}
	
	public <R> R with(Supplier<R> f) {
		try {
			this.enter();
			return f.get();
		}
		finally {
			this.exit();
		}
	}
}
