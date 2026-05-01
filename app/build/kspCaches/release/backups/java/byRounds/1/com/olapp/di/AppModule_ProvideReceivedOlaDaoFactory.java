package com.olapp.di;

import com.olapp.data.local.OlaDatabase;
import com.olapp.data.local.dao.ReceivedOlaDao;
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
public final class AppModule_ProvideReceivedOlaDaoFactory implements Factory<ReceivedOlaDao> {
  private final Provider<OlaDatabase> dbProvider;

  public AppModule_ProvideReceivedOlaDaoFactory(Provider<OlaDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ReceivedOlaDao get() {
    return provideReceivedOlaDao(dbProvider.get());
  }

  public static AppModule_ProvideReceivedOlaDaoFactory create(Provider<OlaDatabase> dbProvider) {
    return new AppModule_ProvideReceivedOlaDaoFactory(dbProvider);
  }

  public static ReceivedOlaDao provideReceivedOlaDao(OlaDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideReceivedOlaDao(db));
  }
}
