package com.kchat.data.network.emergency;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DataStoreEmergencyWipeStore_Factory implements Factory<DataStoreEmergencyWipeStore> {
  private final Provider<Context> contextProvider;

  public DataStoreEmergencyWipeStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataStoreEmergencyWipeStore get() {
    return newInstance(contextProvider.get());
  }

  public static DataStoreEmergencyWipeStore_Factory create(Provider<Context> contextProvider) {
    return new DataStoreEmergencyWipeStore_Factory(contextProvider);
  }

  public static DataStoreEmergencyWipeStore newInstance(Context context) {
    return new DataStoreEmergencyWipeStore(context);
  }
}
