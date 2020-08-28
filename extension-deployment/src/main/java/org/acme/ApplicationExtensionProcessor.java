package org.acme;

import io.quarkus.arc.deployment.ContextRegistrarBuildItem;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class ApplicationExtensionProcessor {
	static final String FEATURE_NAME = "app-extension";

	@BuildStep
	FeatureBuildItem createFeatureItem() {
		return new FeatureBuildItem(FEATURE_NAME);
	}

	@BuildStep
	public void transactionContext(
			BuildProducer<ContextRegistrarBuildItem> contextRegistry) {

		contextRegistry.produce(new ContextRegistrarBuildItem(new ContextRegistrar() {
			@Override
			public void register(RegistrationContext registrationContext) {
				//@SuppressWarnings("unchecked")
				registrationContext.configure(CallScoped.class).normal().contextClass(CallScopeContext.class) // it needs to be of type InjectableContext...
						.done();
			}
		}, CallScoped.class));
	}
}
