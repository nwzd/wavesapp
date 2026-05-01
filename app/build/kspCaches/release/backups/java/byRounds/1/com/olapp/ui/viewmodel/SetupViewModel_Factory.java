package com.olapp.ui.viewmodel;

import android.content.Context;
import com.olapp.data.preferences.AppPreferences;
import com.olapp.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SetupViewModel_Factory implements Factory<SetupViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  public SetupViewModel_Factory(Provider<Context> contextProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    this.contextProvider = contextProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  @Override
  public SetupViewModel get() {
    return newInstance(contextProvider.get(), userRepositoryProvider.get(), appPreferencesProvider.get());
  }

  public static SetupViewModel_Factory create(Provider<Context> contextProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    return new SetupViewModel_Factory(contextProvider, userRepositoryProvider, appPreferencesProvider);
  }

  public static SetupViewModel newInstance(Context context, UserRepository userRepository,
      AppPreferences appPreferences) {
    return new SetupViewModel(context, userRepository, appPreferences);
  }
}
