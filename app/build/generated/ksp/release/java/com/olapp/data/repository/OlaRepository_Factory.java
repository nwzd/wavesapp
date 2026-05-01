package com.olapp.data.repository;

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
    "cast"
})
public final class OlaRepository_Factory implements Factory<OlaRepository> {
  @Override
  public OlaRepository get() {
    return newInstance();
  }

  public static OlaRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OlaRepository newInstance() {
    return new OlaRepository();
  }

  private static final class InstanceHolder {
    private static final OlaRepository_Factory INSTANCE = new OlaRepository_Factory();
  }
}
