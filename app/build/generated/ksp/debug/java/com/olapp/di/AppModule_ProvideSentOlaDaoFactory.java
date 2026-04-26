package com.olapp.di;

import com.olapp.data.local.OlaDatabase;
import com.olapp.data.local.dao.SentOlaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideSentOlaDaoFactory implements Factory<SentOlaDao> {
  private final Provider<OlaDatabase> dbProvider;

  public AppModule_ProvideSentOlaDaoFactory(Provider<OlaDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SentOlaDao get() {
    return provideSentOlaDao(dbProvider.get());
  }

  public static AppModule_ProvideSentOlaDaoFactory create(Provider<OlaDatabase> dbProvider) {
    return new AppModule_ProvideSentOlaDaoFactory(dbProvider);
  }

  public static SentOlaDao provideSentOlaDao(OlaDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSentOlaDao(db));
  }
}
