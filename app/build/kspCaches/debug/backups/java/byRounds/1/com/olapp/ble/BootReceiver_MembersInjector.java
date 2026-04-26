package com.olapp.ble;

import com.olapp.data.preferences.AppPreferences;
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
public final class BootReceiver_MembersInjector implements MembersInjector<BootReceiver> {
  private final Provider<AppPreferences> appPreferencesProvider;

  public BootReceiver_MembersInjector(Provider<AppPreferences> appPreferencesProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
  }

  public static MembersInjector<BootReceiver> create(
      Provider<AppPreferences> appPreferencesProvider) {
    return new BootReceiver_MembersInjector(appPreferencesProvider);
  }

  @Override
  public void injectMembers(BootReceiver instance) {
    injectAppPreferences(instance, appPreferencesProvider.get());
  }

  @InjectedFieldSignature("com.olapp.ble.BootReceiver.appPreferences")
  public static void injectAppPreferences(BootReceiver instance, AppPreferences appPreferences) {
    instance.appPreferences = appPreferences;
  }
}
