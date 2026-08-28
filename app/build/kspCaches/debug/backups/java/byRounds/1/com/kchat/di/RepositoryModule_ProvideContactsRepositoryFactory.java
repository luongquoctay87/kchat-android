package com.kchat.di;

import com.kchat.data.fake.FakeContactsRepository;
import com.kchat.data.network.repository.NetworkContactsRepository;
import com.kchat.data.repository.ContactsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "cast",
    "deprecation"
})
public final class RepositoryModule_ProvideContactsRepositoryFactory implements Factory<ContactsRepository> {
  private final Provider<FakeContactsRepository> fakeProvider;

  private final Provider<NetworkContactsRepository> networkProvider;

  public RepositoryModule_ProvideContactsRepositoryFactory(
      Provider<FakeContactsRepository> fakeProvider,
      Provider<NetworkContactsRepository> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public ContactsRepository get() {
    return provideContactsRepository(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideContactsRepositoryFactory create(
      Provider<FakeContactsRepository> fakeProvider,
      Provider<NetworkContactsRepository> networkProvider) {
    return new RepositoryModule_ProvideContactsRepositoryFactory(fakeProvider, networkProvider);
  }

  public static ContactsRepository provideContactsRepository(FakeContactsRepository fake,
      NetworkContactsRepository network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideContactsRepository(fake, network));
  }
}
