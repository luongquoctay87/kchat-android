package com.kchat.data.fake;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class NoOpIncomingMessageNotifier_Factory implements Factory<NoOpIncomingMessageNotifier> {
  @Override
  public NoOpIncomingMessageNotifier get() {
    return newInstance();
  }

  public static NoOpIncomingMessageNotifier_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NoOpIncomingMessageNotifier newInstance() {
    return new NoOpIncomingMessageNotifier();
  }

  private static final class InstanceHolder {
    private static final NoOpIncomingMessageNotifier_Factory INSTANCE = new NoOpIncomingMessageNotifier_Factory();
  }
}
