package com.olapp.di;

import com.olapp.data.local.OlaDatabase;
import com.olapp.data.local.dao.BlockedUserDao;
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
public final class AppModule_ProvideBlockedUserDaoFactory implements Factory<BlockedUserDao> {
  private final Provider<OlaDatabase> dbProvider;

  public AppModule_ProvideBlockedUserDaoFactory(Provider<OlaDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BlockedUserDao get() {
    return provideBlockedUserDao(dbProvider.get());
  }

  public static AppModule_ProvideBlockedUserDaoFactory create(Provider<OlaDatabase> dbProvider) {
    return new AppModule_ProvideBlockedUserDaoFactory(dbProvider);
  }

  public static BlockedUserDao provideBlockedUserDao(OlaDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBlockedUserDao(db));
  }
}
