package com.github.dynamicextensionsalfresco.workflow.activiti;

import java.beans.ExceptionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Detects workflow tasks from Spring beans implementing the {@link DelegateTask} or {@link TaskListener} interface.
 *
 * Still links components to the {@link WorkflowTaskRegistry} for backwards compatibility, but delegate expressions are
 * now the recommended way of referencing components from bpm files.
 *
 * @author Laurent Van der Linden
 */
public class WorkflowTaskRegistrar implements InitializingBean, ApplicationContextAware, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(WorkflowTaskRegistrar.class);

    private ApplicationContext applicationContext;

    @NotNull
    private final Map<String, Object> activitiBeanRegistry;
    @NotNull
    private final WorkflowTaskRegistry workflowTaskRegistry;

    public WorkflowTaskRegistrar(@NotNull Map<String, Object> activitiBeanRegistry,
            @NotNull WorkflowTaskRegistry workflowTaskRegistry) {
        Intrinsics.checkParameterIsNotNull(activitiBeanRegistry, "activitiBeanRegistry");
        Intrinsics.checkParameterIsNotNull(workflowTaskRegistry, "workflowTaskRegistry");

        this.activitiBeanRegistry = activitiBeanRegistry;
        this.workflowTaskRegistry = workflowTaskRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        for (Class<?> type : workflowDelegateTypes()) {
            Map<String, ?> delegates = applicationContext.getBeansOfType(type);
            delegates.forEach((name, bean) -> {
                Object existing = activitiBeanRegistry.put(name, bean);
                if (existing != null) {
                    logger.warn("replaced existing {} with name {}", existing.getClass(), name);
                }
                logger.debug("registered Bundle component {} (type: {}) -> {}", name, type, bean);

                if (bean instanceof JavaDelegate) {
                    this.workflowTaskRegistry.registerDelegate(name, (JavaDelegate) bean);
                } else if (bean instanceof TaskListener) {
                    this.workflowTaskRegistry.registerTaskListener(name, (TaskListener) bean);
                } else if (bean instanceof ExecutionListener) {
                    this.workflowTaskRegistry.registerExecutionListener(name, (ExecutionListener) bean);
                }
            });
        }
    }

    @Override
    public void destroy() {
        for (Class<?> type : workflowDelegateTypes()) {
            Map<String, ?> delegates = applicationContext.getBeansOfType(type);
            delegates.forEach((name, bean) -> {
                activitiBeanRegistry.remove(name);
                logger.debug("unregistered Bundle component {} (type: {}) -> {}", name, type, bean);

                if (bean instanceof JavaDelegate) {
                    this.workflowTaskRegistry.unregisterDelegate(name);
                } else if (bean instanceof TaskListener) {
                    this.workflowTaskRegistry.unregisterTaskListener(name);
                } else if (bean instanceof ExecutionListener) {
                    this.workflowTaskRegistry.unregisterExecutionListener(name);
                }
            });
        }
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        Intrinsics.checkParameterIsNotNull(applicationContext, "applicationContext");
        this.applicationContext = applicationContext;
    }

    @NotNull
    public final Map getActivitiBeanRegistry() {
        return this.activitiBeanRegistry;
    }

    @NotNull
    public final WorkflowTaskRegistry getWorkflowTaskRegistry() {
        return this.workflowTaskRegistry;
    }

    private List<Class<?>> workflowDelegateTypes() {
        return Arrays.asList(JavaDelegate.class, TaskListener.class, ExceptionListener.class);
    }
}
