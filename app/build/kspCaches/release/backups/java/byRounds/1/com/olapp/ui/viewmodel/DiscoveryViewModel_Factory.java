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
public final class DiscoveryViewModel_Factory implements Factory<DiscoveryViewModel> {
  private final Provider<NearbyManager> nearbyManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<Context> contextProvider;

  public DiscoveryViewModel_Factory(Provider<NearbyManager> nearbyManagerProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<Context> contextProvider) {
    this.nearbyManagerProvider = nearbyManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public DiscoveryViewModel get() {
    return newInstance(nearbyManagerProvider.get(), userRepositoryProvider.get(), contextProvider.get());
  }

  public static DiscoveryViewModel_Factory create(Provider<NearbyManager> nearbyManagerProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<Context> contextProvider) {
    return new DiscoveryViewModel_Factory(nearbyManagerProvider, userRepositoryProvider, contextProvider);
  }

  public static DiscoveryViewModel newInstance(NearbyManager nearbyManager,
      UserRepository userRepository, Context context) {
    return new DiscoveryViewModel(nearbyManager, userRepository, context);
  }
}
