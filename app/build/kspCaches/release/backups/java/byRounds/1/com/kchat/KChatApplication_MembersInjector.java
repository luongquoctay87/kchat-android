package com.kchat;

import androidx.hilt.work.HiltWorkerFactory;
import coil.ImageLoader;
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
    "cast",
    "deprecation"
})
public final class KChatApplication_MembersInjector implements MembersInjector<KChatApplication> {
  private final Provider<ImageLoader> imageLoaderProvider;

  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public KChatApplication_MembersInjector(Provider<ImageLoader> imageLoaderProvider,
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.imageLoaderProvider = imageLoaderProvider;
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<KChatApplication> create(Provider<ImageLoader> imageLoaderProvider,
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new KChatApplication_MembersInjector(imageLoaderProvider, workerFactoryProvider);
  }

  @Override
  public void injectMembers(KChatApplication instance) {
    injectImageLoader(instance, imageLoaderProvider.get());
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.kchat.KChatApplication.imageLoader")
  public static void injectImageLoader(KChatApplication instance, ImageLoader imageLoader) {
    instance.imageLoader = imageLoader;
  }

  @InjectedFieldSignature("com.kchat.KChatApplication.workerFactory")
  public static void injectWorkerFactory(KChatApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
