package com.olapp.ble;

import com.olapp.data.preferences.AppPreferences;
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

  private final Provider<AppPreferences> appPreferencesProvider;

  public BleForegroundService_MembersInjector(Provider<NearbyManager> nearbyManagerProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    this.nearbyManagerProvider = nearbyManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  public static MembersInjector<BleForegroundService> create(
      Provider<NearbyManager> nearbyManagerProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    return new BleForegroundService_MembersInjector(nearbyManagerProvider, userRepositoryProvider, appPreferencesProvider);
  }

  @Override
  public void injectMembers(BleForegroundService instance) {
    injectNearbyManager(instance, nearbyManagerProvider.get());
    injectUserRepository(instance, userRepositoryProvider.get());
    injectAppPreferences(instance, appPreferencesProvider.get());
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

  @InjectedFieldSignature("com.olapp.ble.BleForegroundService.appPreferences")
  public static void injectAppPreferences(BleForegroundService instance,
      AppPreferences appPreferences) {
    instance.appPreferences = appPreferences;
  }
}
