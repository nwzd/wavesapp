package com.olapp.di;

import com.olapp.data.local.OlaDatabase;
import com.olapp.data.local.dao.MatchDao;
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
public final class AppModule_ProvideMatchDaoFactory implements Factory<MatchDao> {
  private final Provider<OlaDatabase> dbProvider;

  public AppModule_ProvideMatchDaoFactory(Provider<OlaDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MatchDao get() {
    return provideMatchDao(dbProvider.get());
  }

  public static AppModule_ProvideMatchDaoFactory create(Provider<OlaDatabase> dbProvider) {
    return new AppModule_ProvideMatchDaoFactory(dbProvider);
  }

  public static MatchDao provideMatchDao(OlaDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMatchDao(db));
  }
}
