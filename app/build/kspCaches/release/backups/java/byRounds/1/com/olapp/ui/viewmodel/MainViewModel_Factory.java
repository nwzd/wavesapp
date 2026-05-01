package com.olapp.ui.viewmodel;

import com.olapp.data.preferences.AppPreferences;
import com.olapp.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<AppPreferences> appPreferencesProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public MainViewModel_Factory(Provider<AppPreferences> appPreferencesProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(appPreferencesProvider.get(), userRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(Provider<AppPreferences> appPreferencesProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new MainViewModel_Factory(appPreferencesProvider, userRepositoryProvider);
  }

  public static MainViewModel newInstance(AppPreferences appPreferences,
      UserRepository userRepository) {
    return new MainViewModel(appPreferences, userRepository);
  }
}
