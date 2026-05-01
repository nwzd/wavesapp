package com.olapp.data.repository;

import com.olapp.data.local.dao.BlockedUserDao;
import com.olapp.data.local.dao.MatchDao;
import com.olapp.data.local.dao.ReceivedOlaDao;
import com.olapp.data.local.dao.SentOlaDao;
import com.olapp.data.local.dao.UserProfileDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class UserRepository_Factory implements Factory<UserRepository> {
  private final Provider<UserProfileDao> userProfileDaoProvider;

  private final Provider<ReceivedOlaDao> receivedOlaDaoProvider;

  private final Provider<SentOlaDao> sentOlaDaoProvider;

  private final Provider<MatchDao> matchDaoProvider;

  private final Provider<BlockedUserDao> blockedUserDaoProvider;

  public UserRepository_Factory(Provider<UserProfileDao> userProfileDaoProvider,
      Provider<ReceivedOlaDao> receivedOlaDaoProvider, Provider<SentOlaDao> sentOlaDaoProvider,
      Provider<MatchDao> matchDaoProvider, Provider<BlockedUserDao> blockedUserDaoProvider) {
    this.userProfileDaoProvider = userProfileDaoProvider;
    this.receivedOlaDaoProvider = receivedOlaDaoProvider;
    this.sentOlaDaoProvider = sentOlaDaoProvider;
    this.matchDaoProvider = matchDaoProvider;
    this.blockedUserDaoProvider = blockedUserDaoProvider;
  }

  @Override
  public UserRepository get() {
    return newInstance(userProfileDaoProvider.get(), receivedOlaDaoProvider.get(), sentOlaDaoProvider.get(), matchDaoProvider.get(), blockedUserDaoProvider.get());
  }

  public static UserRepository_Factory create(Provider<UserProfileDao> userProfileDaoProvider,
      Provider<ReceivedOlaDao> receivedOlaDaoProvider, Provider<SentOlaDao> sentOlaDaoProvider,
      Provider<MatchDao> matchDaoProvider, Provider<BlockedUserDao> blockedUserDaoProvider) {
    return new UserRepository_Factory(userProfileDaoProvider, receivedOlaDaoProvider, sentOlaDaoProvider, matchDaoProvider, blockedUserDaoProvider);
  }

  public static UserRepository newInstance(UserProfileDao userProfileDao,
      ReceivedOlaDao receivedOlaDao, SentOlaDao sentOlaDao, MatchDao matchDao,
      BlockedUserDao blockedUserDao) {
    return new UserRepository(userProfileDao, receivedOlaDao, sentOlaDao, matchDao, blockedUserDao);
  }
}
