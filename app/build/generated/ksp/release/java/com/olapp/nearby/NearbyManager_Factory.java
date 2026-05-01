package com.olapp.nearby;

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
    "cast"
})
public final class NearbyManager_Factory implements Factory<NearbyManager> {
  private final Provider<Context> contextProvider;

  public NearbyManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NearbyManager get() {
    return newInstance(contextProvider.get());
  }

  public static NearbyManager_Factory create(Provider<Context> contextProvider) {
    return new NearbyManager_Factory(contextProvider);
  }

  public static NearbyManager newInstance(Context context) {
    return new NearbyManager(context);
  }
}
