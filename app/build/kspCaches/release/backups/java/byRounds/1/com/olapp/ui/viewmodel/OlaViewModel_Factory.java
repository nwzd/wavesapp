package com.olapp.ui.viewmodel;

import android.content.Context;
import com.olapp.data.repository.UserRepository;
import com.olapp.nearby.NearbyManager;
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
public final class OlaViewModel_Factory implements Factory<OlaViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<NearbyManager> nearbyManagerProvider;

  private final Provider<Context> contextProvider;

  public OlaViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<NearbyManager> nearbyManagerProvider, Provider<Context> contextProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.nearbyManagerProvider = nearbyManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public OlaViewModel get() {
    return newInstance(userRepositoryProvider.get(), nearbyManagerProvider.get(), contextProvider.get());
  }

  public static OlaViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<NearbyManager> nearbyManagerProvider, Provider<Context> contextProvider) {
    return new OlaViewModel_Factory(userRepositoryProvider, nearbyManagerProvider, contextProvider);
  }

  public static OlaViewModel newInstance(UserRepository userRepository, NearbyManager nearbyManager,
      Context context) {
    return new OlaViewModel(userRepository, nearbyManager, context);
  }
}
