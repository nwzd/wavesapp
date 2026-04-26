package com.olapp.ble;

import com.olapp.data.repository.UserRepository;
import com.olapp.nearby.NearbyManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class BleForegroundService_MembersInjector implements MembersInjector<BleForegroundService> {
  private final Provider<NearbyManager> nearbyManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public BleForegroundService_MembersInjector(Provider<NearbyManager> nearbyManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.nearbyManagerProvider = nearbyManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  public static MembersInjector<BleForegroundService> create(
      Provider<NearbyManager> nearbyManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new BleForegroundService_MembersInjector(nearbyManagerProvider, userRepositoryProvider);
  }

  @Override
  public void injectMembers(BleForegroundService instance) {
    injectNearbyManager(instance, nearbyManagerProvider.get());
    injectUserRepository(instance, userRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.olapp.ble.BleForegroundService.nearbyManager")
  public static void injectNearbyManager(BleForegroundService instance,
      NearbyManager nearbyManager) {
    instance.nearbyManager = nearbyManager;
  }

  @InjectedFieldSignature("com.olapp.ble.BleForegroundService.userRepository")
  public static void injectUserRepository(BleForegroundService instance,
      UserRepository userRepository) {
    instance.userRepository = userRepository;
  }
}
