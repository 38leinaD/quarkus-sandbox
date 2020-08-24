package org.acme;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class ApplicationExtensionProcessor {
	static final String FEATURE_NAME = "app-extension";

	@BuildStep
	FeatureBuildItem createFeatureItem() {
		System.out.println("++ createFeatureItem");
		return new FeatureBuildItem(FEATURE_NAME);
	}
}
