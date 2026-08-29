package com.kchat;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import coil.ImageLoader;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.kchat.core.navigation.viewmodel.CallViewModel;
import com.kchat.core.navigation.viewmodel.CallViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.ChatListViewModel;
import com.kchat.core.navigation.viewmodel.ChatListViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.ChatRoomViewModel;
import com.kchat.core.navigation.viewmodel.ChatRoomViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.ContactsViewModel;
import com.kchat.core.navigation.viewmodel.ContactsViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.CreateGroupViewModel;
import com.kchat.core.navigation.viewmodel.CreateGroupViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.DevicesViewModel;
import com.kchat.core.navigation.viewmodel.DevicesViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.ForgotPasswordViewModel;
import com.kchat.core.navigation.viewmodel.ForgotPasswordViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.GroupInfoViewModel;
import com.kchat.core.navigation.viewmodel.GroupInfoViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.InChatSearchViewModel;
import com.kchat.core.navigation.viewmodel.InChatSearchViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.IncomingCallViewModel;
import com.kchat.core.navigation.viewmodel.IncomingCallViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.LoginViewModel;
import com.kchat.core.navigation.viewmodel.LoginViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.PinLockViewModel;
import com.kchat.core.navigation.viewmodel.PinLockViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.PinSettingsViewModel;
import com.kchat.core.navigation.viewmodel.PinSettingsViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.RegisterViewModel;
import com.kchat.core.navigation.viewmodel.RegisterViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.ResetPasswordViewModel;
import com.kchat.core.navigation.viewmodel.ResetPasswordViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.SessionViewModel;
import com.kchat.core.navigation.viewmodel.SessionViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.SettingsViewModel;
import com.kchat.core.navigation.viewmodel.SettingsViewModel_HiltModules;
import com.kchat.core.navigation.viewmodel.VerifyResetOtpViewModel;
import com.kchat.core.navigation.viewmodel.VerifyResetOtpViewModel_HiltModules;
import com.kchat.data.fake.FakeAuthRepository;
import com.kchat.data.fake.FakeCallRepository;
import com.kchat.data.fake.FakeChatRepository;
import com.kchat.data.fake.FakeContactsRepository;
import com.kchat.data.fake.FakeDeviceTokenStore;
import com.kchat.data.fake.FakePinLockStore;
import com.kchat.data.fake.FakePushTokenRegistrar;
import com.kchat.data.fake.FakeRealtimeCoordinator;
import com.kchat.data.fake.FakeSettingsRepository;
import com.kchat.data.fake.FakeTokenStore;
import com.kchat.data.fake.NoOpIncomingMessageNotifier;
import com.kchat.data.fake.NoOpPushTokenSync;
import com.kchat.data.local.ChatLocalDataSource;
import com.kchat.data.local.KChatDatabase;
import com.kchat.data.local.dao.MessageDao;
import com.kchat.data.local.dao.RoomDao;
import com.kchat.data.network.api.KChatApi;
import com.kchat.data.network.auth.DataStoreDeviceTokenStore;
import com.kchat.data.network.auth.DataStorePinLockStore;
import com.kchat.data.network.auth.DataStoreTokenStore;
import com.kchat.data.network.auth.TokenAuthenticator;
import com.kchat.data.network.emergency.DataStoreEmergencyWipeStore;
import com.kchat.data.network.emergency.EmergencyWipeCoordinatorImpl;
import com.kchat.data.network.repository.NetworkAuthRepository;
import com.kchat.data.network.repository.NetworkCallRepository;
import com.kchat.data.network.repository.NetworkChatRepository;
import com.kchat.data.network.repository.NetworkContactsRepository;
import com.kchat.data.network.repository.NetworkPushTokenRegistrar;
import com.kchat.data.network.repository.NetworkRealtimeCoordinator;
import com.kchat.data.network.repository.NetworkSettingsRepository;
import com.kchat.data.network.ws.KChatWebSocketClient;
import com.kchat.data.repository.AccessTokenHolder;
import com.kchat.data.repository.ActiveRoomTracker;
import com.kchat.data.repository.AppForegroundTracker;
import com.kchat.data.repository.AuthRepository;
import com.kchat.data.repository.CallRepository;
import com.kchat.data.repository.CallSignalBus;
import com.kchat.data.repository.ChatRepository;
import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.DeviceTokenStore;
import com.kchat.data.repository.EmergencyWipeCoordinator;
import com.kchat.data.repository.EmergencyWipeStore;
import com.kchat.data.repository.IncomingMessageNotifier;
import com.kchat.data.repository.PinLockStore;
import com.kchat.data.repository.PushNavigationStore;
import com.kchat.data.repository.PushTokenRegistrar;
import com.kchat.data.repository.PushTokenSync;
import com.kchat.data.repository.RealtimeCoordinator;
import com.kchat.data.repository.SessionCoordinator;
import com.kchat.data.repository.SettingsRepository;
import com.kchat.data.repository.TokenStore;
import com.kchat.data.repository.TypingStateStore;
import com.kchat.di.DatabaseModule_ProvideDatabaseFactory;
import com.kchat.di.DatabaseModule_ProvideMessageDaoFactory;
import com.kchat.di.DatabaseModule_ProvideRoomDaoFactory;
import com.kchat.di.NetworkModule_ProvideApiBaseUrlFactory;
import com.kchat.di.NetworkModule_ProvideImageLoaderFactory;
import com.kchat.di.NetworkModule_ProvideKChatApiFactory;
import com.kchat.di.NetworkModule_ProvideOkHttpClientFactory;
import com.kchat.di.NetworkModule_ProvideRetrofitFactory;
import com.kchat.di.NetworkModule_ProvideTokenAuthenticatorFactory;
import com.kchat.di.NetworkModule_ProvideWebSocketClientFactory;
import com.kchat.di.NetworkModule_ProvideWsOkHttpClientFactory;
import com.kchat.di.RepositoryModule_ProvideAccessTokenHolderFactory;
import com.kchat.di.RepositoryModule_ProvideActiveRoomTrackerFactory;
import com.kchat.di.RepositoryModule_ProvideAppForegroundTrackerFactory;
import com.kchat.di.RepositoryModule_ProvideAuthRepositoryFactory;
import com.kchat.di.RepositoryModule_ProvideCallRepositoryFactory;
import com.kchat.di.RepositoryModule_ProvideCallSignalBusFactory;
import com.kchat.di.RepositoryModule_ProvideChatRepositoryFactory;
import com.kchat.di.RepositoryModule_ProvideContactsRepositoryFactory;
import com.kchat.di.RepositoryModule_ProvideDeviceTokenStoreFactory;
import com.kchat.di.RepositoryModule_ProvideEmergencyWipeCoordinatorFactory;
import com.kchat.di.RepositoryModule_ProvideEmergencyWipeStoreFactory;
import com.kchat.di.RepositoryModule_ProvideIncomingMessageNotifierFactory;
import com.kchat.di.RepositoryModule_ProvidePinLockStoreFactory;
import com.kchat.di.RepositoryModule_ProvidePushNavigationStoreFactory;
import com.kchat.di.RepositoryModule_ProvidePushTokenRegistrarFactory;
import com.kchat.di.RepositoryModule_ProvidePushTokenSyncFactory;
import com.kchat.di.RepositoryModule_ProvideRealtimeCoordinatorFactory;
import com.kchat.di.RepositoryModule_ProvideSessionCoordinatorFactory;
import com.kchat.di.RepositoryModule_ProvideSettingsRepositoryFactory;
import com.kchat.di.RepositoryModule_ProvideTokenStoreFactory;
import com.kchat.di.RepositoryModule_ProvideTypingStateStoreFactory;
import com.kchat.push.FcmTokenHandler;
import com.kchat.push.FirebasePushTokenSync;
import com.kchat.push.IncomingMessageNotifierImpl;
import com.kchat.push.KChatFirebaseMessagingService;
import com.kchat.push.KChatFirebaseMessagingService_MembersInjector;
import com.kchat.push.PushNotificationHelper;
import com.kchat.work.LocalCacheCleanupWorker;
import com.kchat.work.LocalCacheCleanupWorker_AssistedFactory;
import com.kchat.work.WorkManagerLocalCacheCleanupScheduler;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerKChatApplication_HiltComponents_SingletonC {
  private DaggerKChatApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public KChatApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements KChatApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements KChatApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements KChatApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements KChatApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements KChatApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements KChatApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements KChatApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public KChatApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends KChatApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends KChatApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends KChatApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends KChatApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(18).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_CallViewModel, CallViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ChatListViewModel, ChatListViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ChatRoomViewModel, ChatRoomViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ContactsViewModel, ContactsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_CreateGroupViewModel, CreateGroupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_DevicesViewModel, DevicesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ForgotPasswordViewModel, ForgotPasswordViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_GroupInfoViewModel, GroupInfoViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_InChatSearchViewModel, InChatSearchViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_IncomingCallViewModel, IncomingCallViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_LoginViewModel, LoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_PinLockViewModel, PinLockViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_PinSettingsViewModel, PinSettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_RegisterViewModel, RegisterViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ResetPasswordViewModel, ResetPasswordViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_SessionViewModel, SessionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_VerifyResetOtpViewModel, VerifyResetOtpViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectPushNavigationStore(instance, singletonCImpl.providePushNavigationStoreProvider.get());
      MainActivity_MembersInjector.injectPushNotificationHelper(instance, singletonCImpl.pushNotificationHelperProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_kchat_core_navigation_viewmodel_RegisterViewModel = "com.kchat.core.navigation.viewmodel.RegisterViewModel";

      static String com_kchat_core_navigation_viewmodel_ChatRoomViewModel = "com.kchat.core.navigation.viewmodel.ChatRoomViewModel";

      static String com_kchat_core_navigation_viewmodel_PinSettingsViewModel = "com.kchat.core.navigation.viewmodel.PinSettingsViewModel";

      static String com_kchat_core_navigation_viewmodel_DevicesViewModel = "com.kchat.core.navigation.viewmodel.DevicesViewModel";

      static String com_kchat_core_navigation_viewmodel_VerifyResetOtpViewModel = "com.kchat.core.navigation.viewmodel.VerifyResetOtpViewModel";

      static String com_kchat_core_navigation_viewmodel_InChatSearchViewModel = "com.kchat.core.navigation.viewmodel.InChatSearchViewModel";

      static String com_kchat_core_navigation_viewmodel_SettingsViewModel = "com.kchat.core.navigation.viewmodel.SettingsViewModel";

      static String com_kchat_core_navigation_viewmodel_ChatListViewModel = "com.kchat.core.navigation.viewmodel.ChatListViewModel";

      static String com_kchat_core_navigation_viewmodel_GroupInfoViewModel = "com.kchat.core.navigation.viewmodel.GroupInfoViewModel";

      static String com_kchat_core_navigation_viewmodel_CallViewModel = "com.kchat.core.navigation.viewmodel.CallViewModel";

      static String com_kchat_core_navigation_viewmodel_ContactsViewModel = "com.kchat.core.navigation.viewmodel.ContactsViewModel";

      static String com_kchat_core_navigation_viewmodel_ForgotPasswordViewModel = "com.kchat.core.navigation.viewmodel.ForgotPasswordViewModel";

      static String com_kchat_core_navigation_viewmodel_LoginViewModel = "com.kchat.core.navigation.viewmodel.LoginViewModel";

      static String com_kchat_core_navigation_viewmodel_CreateGroupViewModel = "com.kchat.core.navigation.viewmodel.CreateGroupViewModel";

      static String com_kchat_core_navigation_viewmodel_IncomingCallViewModel = "com.kchat.core.navigation.viewmodel.IncomingCallViewModel";

      static String com_kchat_core_navigation_viewmodel_PinLockViewModel = "com.kchat.core.navigation.viewmodel.PinLockViewModel";

      static String com_kchat_core_navigation_viewmodel_SessionViewModel = "com.kchat.core.navigation.viewmodel.SessionViewModel";

      static String com_kchat_core_navigation_viewmodel_ResetPasswordViewModel = "com.kchat.core.navigation.viewmodel.ResetPasswordViewModel";

      @KeepFieldType
      RegisterViewModel com_kchat_core_navigation_viewmodel_RegisterViewModel2;

      @KeepFieldType
      ChatRoomViewModel com_kchat_core_navigation_viewmodel_ChatRoomViewModel2;

      @KeepFieldType
      PinSettingsViewModel com_kchat_core_navigation_viewmodel_PinSettingsViewModel2;

      @KeepFieldType
      DevicesViewModel com_kchat_core_navigation_viewmodel_DevicesViewModel2;

      @KeepFieldType
      VerifyResetOtpViewModel com_kchat_core_navigation_viewmodel_VerifyResetOtpViewModel2;

      @KeepFieldType
      InChatSearchViewModel com_kchat_core_navigation_viewmodel_InChatSearchViewModel2;

      @KeepFieldType
      SettingsViewModel com_kchat_core_navigation_viewmodel_SettingsViewModel2;

      @KeepFieldType
      ChatListViewModel com_kchat_core_navigation_viewmodel_ChatListViewModel2;

      @KeepFieldType
      GroupInfoViewModel com_kchat_core_navigation_viewmodel_GroupInfoViewModel2;

      @KeepFieldType
      CallViewModel com_kchat_core_navigation_viewmodel_CallViewModel2;

      @KeepFieldType
      ContactsViewModel com_kchat_core_navigation_viewmodel_ContactsViewModel2;

      @KeepFieldType
      ForgotPasswordViewModel com_kchat_core_navigation_viewmodel_ForgotPasswordViewModel2;

      @KeepFieldType
      LoginViewModel com_kchat_core_navigation_viewmodel_LoginViewModel2;

      @KeepFieldType
      CreateGroupViewModel com_kchat_core_navigation_viewmodel_CreateGroupViewModel2;

      @KeepFieldType
      IncomingCallViewModel com_kchat_core_navigation_viewmodel_IncomingCallViewModel2;

      @KeepFieldType
      PinLockViewModel com_kchat_core_navigation_viewmodel_PinLockViewModel2;

      @KeepFieldType
      SessionViewModel com_kchat_core_navigation_viewmodel_SessionViewModel2;

      @KeepFieldType
      ResetPasswordViewModel com_kchat_core_navigation_viewmodel_ResetPasswordViewModel2;
    }
  }

  private static final class ViewModelCImpl extends KChatApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<CallViewModel> callViewModelProvider;

    private Provider<ChatListViewModel> chatListViewModelProvider;

    private Provider<ChatRoomViewModel> chatRoomViewModelProvider;

    private Provider<ContactsViewModel> contactsViewModelProvider;

    private Provider<CreateGroupViewModel> createGroupViewModelProvider;

    private Provider<DevicesViewModel> devicesViewModelProvider;

    private Provider<ForgotPasswordViewModel> forgotPasswordViewModelProvider;

    private Provider<GroupInfoViewModel> groupInfoViewModelProvider;

    private Provider<InChatSearchViewModel> inChatSearchViewModelProvider;

    private Provider<IncomingCallViewModel> incomingCallViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<PinLockViewModel> pinLockViewModelProvider;

    private Provider<PinSettingsViewModel> pinSettingsViewModelProvider;

    private Provider<RegisterViewModel> registerViewModelProvider;

    private Provider<ResetPasswordViewModel> resetPasswordViewModelProvider;

    private Provider<SessionViewModel> sessionViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<VerifyResetOtpViewModel> verifyResetOtpViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.callViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.chatListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chatRoomViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.contactsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.createGroupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.devicesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.forgotPasswordViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.groupInfoViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.inChatSearchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.incomingCallViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.pinLockViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.pinSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.resetPasswordViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.sessionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.verifyResetOtpViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(18).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_CallViewModel, ((Provider) callViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ChatListViewModel, ((Provider) chatListViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ChatRoomViewModel, ((Provider) chatRoomViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ContactsViewModel, ((Provider) contactsViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_CreateGroupViewModel, ((Provider) createGroupViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_DevicesViewModel, ((Provider) devicesViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ForgotPasswordViewModel, ((Provider) forgotPasswordViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_GroupInfoViewModel, ((Provider) groupInfoViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_InChatSearchViewModel, ((Provider) inChatSearchViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_IncomingCallViewModel, ((Provider) incomingCallViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_LoginViewModel, ((Provider) loginViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_PinLockViewModel, ((Provider) pinLockViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_PinSettingsViewModel, ((Provider) pinSettingsViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_RegisterViewModel, ((Provider) registerViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_ResetPasswordViewModel, ((Provider) resetPasswordViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_SessionViewModel, ((Provider) sessionViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_kchat_core_navigation_viewmodel_VerifyResetOtpViewModel, ((Provider) verifyResetOtpViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_kchat_core_navigation_viewmodel_ChatListViewModel = "com.kchat.core.navigation.viewmodel.ChatListViewModel";

      static String com_kchat_core_navigation_viewmodel_CreateGroupViewModel = "com.kchat.core.navigation.viewmodel.CreateGroupViewModel";

      static String com_kchat_core_navigation_viewmodel_ForgotPasswordViewModel = "com.kchat.core.navigation.viewmodel.ForgotPasswordViewModel";

      static String com_kchat_core_navigation_viewmodel_VerifyResetOtpViewModel = "com.kchat.core.navigation.viewmodel.VerifyResetOtpViewModel";

      static String com_kchat_core_navigation_viewmodel_RegisterViewModel = "com.kchat.core.navigation.viewmodel.RegisterViewModel";

      static String com_kchat_core_navigation_viewmodel_SessionViewModel = "com.kchat.core.navigation.viewmodel.SessionViewModel";

      static String com_kchat_core_navigation_viewmodel_ContactsViewModel = "com.kchat.core.navigation.viewmodel.ContactsViewModel";

      static String com_kchat_core_navigation_viewmodel_InChatSearchViewModel = "com.kchat.core.navigation.viewmodel.InChatSearchViewModel";

      static String com_kchat_core_navigation_viewmodel_IncomingCallViewModel = "com.kchat.core.navigation.viewmodel.IncomingCallViewModel";

      static String com_kchat_core_navigation_viewmodel_GroupInfoViewModel = "com.kchat.core.navigation.viewmodel.GroupInfoViewModel";

      static String com_kchat_core_navigation_viewmodel_CallViewModel = "com.kchat.core.navigation.viewmodel.CallViewModel";

      static String com_kchat_core_navigation_viewmodel_ResetPasswordViewModel = "com.kchat.core.navigation.viewmodel.ResetPasswordViewModel";

      static String com_kchat_core_navigation_viewmodel_PinSettingsViewModel = "com.kchat.core.navigation.viewmodel.PinSettingsViewModel";

      static String com_kchat_core_navigation_viewmodel_SettingsViewModel = "com.kchat.core.navigation.viewmodel.SettingsViewModel";

      static String com_kchat_core_navigation_viewmodel_PinLockViewModel = "com.kchat.core.navigation.viewmodel.PinLockViewModel";

      static String com_kchat_core_navigation_viewmodel_LoginViewModel = "com.kchat.core.navigation.viewmodel.LoginViewModel";

      static String com_kchat_core_navigation_viewmodel_DevicesViewModel = "com.kchat.core.navigation.viewmodel.DevicesViewModel";

      static String com_kchat_core_navigation_viewmodel_ChatRoomViewModel = "com.kchat.core.navigation.viewmodel.ChatRoomViewModel";

      @KeepFieldType
      ChatListViewModel com_kchat_core_navigation_viewmodel_ChatListViewModel2;

      @KeepFieldType
      CreateGroupViewModel com_kchat_core_navigation_viewmodel_CreateGroupViewModel2;

      @KeepFieldType
      ForgotPasswordViewModel com_kchat_core_navigation_viewmodel_ForgotPasswordViewModel2;

      @KeepFieldType
      VerifyResetOtpViewModel com_kchat_core_navigation_viewmodel_VerifyResetOtpViewModel2;

      @KeepFieldType
      RegisterViewModel com_kchat_core_navigation_viewmodel_RegisterViewModel2;

      @KeepFieldType
      SessionViewModel com_kchat_core_navigation_viewmodel_SessionViewModel2;

      @KeepFieldType
      ContactsViewModel com_kchat_core_navigation_viewmodel_ContactsViewModel2;

      @KeepFieldType
      InChatSearchViewModel com_kchat_core_navigation_viewmodel_InChatSearchViewModel2;

      @KeepFieldType
      IncomingCallViewModel com_kchat_core_navigation_viewmodel_IncomingCallViewModel2;

      @KeepFieldType
      GroupInfoViewModel com_kchat_core_navigation_viewmodel_GroupInfoViewModel2;

      @KeepFieldType
      CallViewModel com_kchat_core_navigation_viewmodel_CallViewModel2;

      @KeepFieldType
      ResetPasswordViewModel com_kchat_core_navigation_viewmodel_ResetPasswordViewModel2;

      @KeepFieldType
      PinSettingsViewModel com_kchat_core_navigation_viewmodel_PinSettingsViewModel2;

      @KeepFieldType
      SettingsViewModel com_kchat_core_navigation_viewmodel_SettingsViewModel2;

      @KeepFieldType
      PinLockViewModel com_kchat_core_navigation_viewmodel_PinLockViewModel2;

      @KeepFieldType
      LoginViewModel com_kchat_core_navigation_viewmodel_LoginViewModel2;

      @KeepFieldType
      DevicesViewModel com_kchat_core_navigation_viewmodel_DevicesViewModel2;

      @KeepFieldType
      ChatRoomViewModel com_kchat_core_navigation_viewmodel_ChatRoomViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.kchat.core.navigation.viewmodel.CallViewModel 
          return (T) new CallViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.provideCallRepositoryProvider.get(), singletonCImpl.provideCallSignalBusProvider.get(), singletonCImpl.provideRealtimeCoordinatorProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.kchat.core.navigation.viewmodel.ChatListViewModel 
          return (T) new ChatListViewModel(singletonCImpl.provideChatRepositoryProvider.get(), singletonCImpl.provideTypingStateStoreProvider.get());

          case 2: // com.kchat.core.navigation.viewmodel.ChatRoomViewModel 
          return (T) new ChatRoomViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.provideChatRepositoryProvider.get(), singletonCImpl.provideActiveRoomTrackerProvider.get(), singletonCImpl.provideRealtimeCoordinatorProvider.get());

          case 3: // com.kchat.core.navigation.viewmodel.ContactsViewModel 
          return (T) new ContactsViewModel(singletonCImpl.provideContactsRepositoryProvider.get());

          case 4: // com.kchat.core.navigation.viewmodel.CreateGroupViewModel 
          return (T) new CreateGroupViewModel(singletonCImpl.provideContactsRepositoryProvider.get(), singletonCImpl.provideChatRepositoryProvider.get());

          case 5: // com.kchat.core.navigation.viewmodel.DevicesViewModel 
          return (T) new DevicesViewModel(singletonCImpl.provideSettingsRepositoryProvider.get());

          case 6: // com.kchat.core.navigation.viewmodel.ForgotPasswordViewModel 
          return (T) new ForgotPasswordViewModel(singletonCImpl.provideAuthRepositoryProvider.get());

          case 7: // com.kchat.core.navigation.viewmodel.GroupInfoViewModel 
          return (T) new GroupInfoViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.provideChatRepositoryProvider.get(), singletonCImpl.provideContactsRepositoryProvider.get());

          case 8: // com.kchat.core.navigation.viewmodel.InChatSearchViewModel 
          return (T) new InChatSearchViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.provideChatRepositoryProvider.get());

          case 9: // com.kchat.core.navigation.viewmodel.IncomingCallViewModel 
          return (T) new IncomingCallViewModel(singletonCImpl.provideCallSignalBusProvider.get());

          case 10: // com.kchat.core.navigation.viewmodel.LoginViewModel 
          return (T) new LoginViewModel(singletonCImpl.provideAuthRepositoryProvider.get(), singletonCImpl.provideSessionCoordinatorProvider.get());

          case 11: // com.kchat.core.navigation.viewmodel.PinLockViewModel 
          return (T) new PinLockViewModel(singletonCImpl.providePinLockStoreProvider.get(), singletonCImpl.provideEmergencyWipeCoordinatorProvider.get(), singletonCImpl.provideAuthRepositoryProvider.get());

          case 12: // com.kchat.core.navigation.viewmodel.PinSettingsViewModel 
          return (T) new PinSettingsViewModel(singletonCImpl.providePinLockStoreProvider.get());

          case 13: // com.kchat.core.navigation.viewmodel.RegisterViewModel 
          return (T) new RegisterViewModel(singletonCImpl.provideAuthRepositoryProvider.get(), singletonCImpl.provideSessionCoordinatorProvider.get());

          case 14: // com.kchat.core.navigation.viewmodel.ResetPasswordViewModel 
          return (T) new ResetPasswordViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.provideAuthRepositoryProvider.get());

          case 15: // com.kchat.core.navigation.viewmodel.SessionViewModel 
          return (T) new SessionViewModel(singletonCImpl.provideAuthRepositoryProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideSessionCoordinatorProvider.get(), singletonCImpl.provideRealtimeCoordinatorProvider.get(), singletonCImpl.providePinLockStoreProvider.get(), singletonCImpl.provideAppForegroundTrackerProvider.get(), singletonCImpl.provideActiveRoomTrackerProvider.get());

          case 16: // com.kchat.core.navigation.viewmodel.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideSettingsRepositoryProvider.get(), singletonCImpl.provideAuthRepositoryProvider.get(), singletonCImpl.providePinLockStoreProvider.get());

          case 17: // com.kchat.core.navigation.viewmodel.VerifyResetOtpViewModel 
          return (T) new VerifyResetOtpViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.provideAuthRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends KChatApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends KChatApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectKChatFirebaseMessagingService(KChatFirebaseMessagingService arg0) {
      injectKChatFirebaseMessagingService2(arg0);
    }

    @CanIgnoreReturnValue
    private KChatFirebaseMessagingService injectKChatFirebaseMessagingService2(
        KChatFirebaseMessagingService instance) {
      KChatFirebaseMessagingService_MembersInjector.injectFcmTokenHandler(instance, singletonCImpl.fcmTokenHandlerProvider.get());
      KChatFirebaseMessagingService_MembersInjector.injectPushNotificationHelper(instance, singletonCImpl.pushNotificationHelperProvider.get());
      KChatFirebaseMessagingService_MembersInjector.injectActiveRoomTracker(instance, singletonCImpl.provideActiveRoomTrackerProvider.get());
      KChatFirebaseMessagingService_MembersInjector.injectAppForegroundTracker(instance, singletonCImpl.provideAppForegroundTrackerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends KChatApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AccessTokenHolder> provideAccessTokenHolderProvider;

    private Provider<FakeTokenStore> fakeTokenStoreProvider;

    private Provider<DataStoreTokenStore> dataStoreTokenStoreProvider;

    private Provider<TokenStore> provideTokenStoreProvider;

    private Provider<FakeDeviceTokenStore> fakeDeviceTokenStoreProvider;

    private Provider<DataStoreDeviceTokenStore> dataStoreDeviceTokenStoreProvider;

    private Provider<DeviceTokenStore> provideDeviceTokenStoreProvider;

    private Provider<TokenAuthenticator> provideTokenAuthenticatorProvider;

    private Provider<ImageLoader> provideImageLoaderProvider;

    private Provider<KChatDatabase> provideDatabaseProvider;

    private Provider<ChatLocalDataSource> chatLocalDataSourceProvider;

    private Provider<LocalCacheCleanupWorker_AssistedFactory> localCacheCleanupWorker_AssistedFactoryProvider;

    private Provider<PushNavigationStore> providePushNavigationStoreProvider;

    private Provider<PushNotificationHelper> pushNotificationHelperProvider;

    private Provider<CallSignalBus> provideCallSignalBusProvider;

    private Provider<FakeCallRepository> fakeCallRepositoryProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<KChatApi> provideKChatApiProvider;

    private Provider<NetworkCallRepository> networkCallRepositoryProvider;

    private Provider<CallRepository> provideCallRepositoryProvider;

    private Provider<FakeRealtimeCoordinator> fakeRealtimeCoordinatorProvider;

    private Provider<OkHttpClient> provideWsOkHttpClientProvider;

    private Provider<KChatWebSocketClient> provideWebSocketClientProvider;

    private Provider<DataStoreEmergencyWipeStore> dataStoreEmergencyWipeStoreProvider;

    private Provider<EmergencyWipeStore> provideEmergencyWipeStoreProvider;

    private Provider<FakeChatRepository> fakeChatRepositoryProvider;

    private Provider<TypingStateStore> provideTypingStateStoreProvider;

    private Provider<String> provideApiBaseUrlProvider;

    private Provider<NetworkChatRepository> networkChatRepositoryProvider;

    private Provider<ChatRepository> provideChatRepositoryProvider;

    private Provider<FakeContactsRepository> fakeContactsRepositoryProvider;

    private Provider<NetworkContactsRepository> networkContactsRepositoryProvider;

    private Provider<ContactsRepository> provideContactsRepositoryProvider;

    private Provider<ActiveRoomTracker> provideActiveRoomTrackerProvider;

    private Provider<NoOpIncomingMessageNotifier> noOpIncomingMessageNotifierProvider;

    private Provider<AppForegroundTracker> provideAppForegroundTrackerProvider;

    private Provider<WorkManagerLocalCacheCleanupScheduler> workManagerLocalCacheCleanupSchedulerProvider;

    private Provider<FakeSettingsRepository> fakeSettingsRepositoryProvider;

    private Provider<NetworkSettingsRepository> networkSettingsRepositoryProvider;

    private Provider<SettingsRepository> provideSettingsRepositoryProvider;

    private Provider<IncomingMessageNotifierImpl> incomingMessageNotifierImplProvider;

    private Provider<IncomingMessageNotifier> provideIncomingMessageNotifierProvider;

    private Provider<NetworkRealtimeCoordinator> networkRealtimeCoordinatorProvider;

    private Provider<RealtimeCoordinator> provideRealtimeCoordinatorProvider;

    private Provider<FakePinLockStore> fakePinLockStoreProvider;

    private Provider<DataStorePinLockStore> dataStorePinLockStoreProvider;

    private Provider<PinLockStore> providePinLockStoreProvider;

    private Provider<FakeAuthRepository> fakeAuthRepositoryProvider;

    private Provider<NetworkAuthRepository> networkAuthRepositoryProvider;

    private Provider<AuthRepository> provideAuthRepositoryProvider;

    private Provider<FakePushTokenRegistrar> fakePushTokenRegistrarProvider;

    private Provider<NetworkPushTokenRegistrar> networkPushTokenRegistrarProvider;

    private Provider<PushTokenRegistrar> providePushTokenRegistrarProvider;

    private Provider<FcmTokenHandler> fcmTokenHandlerProvider;

    private Provider<FirebasePushTokenSync> firebasePushTokenSyncProvider;

    private Provider<NoOpPushTokenSync> noOpPushTokenSyncProvider;

    private Provider<PushTokenSync> providePushTokenSyncProvider;

    private Provider<SessionCoordinator> provideSessionCoordinatorProvider;

    private Provider<EmergencyWipeCoordinatorImpl> emergencyWipeCoordinatorImplProvider;

    private Provider<EmergencyWipeCoordinator> provideEmergencyWipeCoordinatorProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);
      initialize3(applicationContextModuleParam);

    }

    private RoomDao roomDao() {
      return DatabaseModule_ProvideRoomDaoFactory.provideRoomDao(provideDatabaseProvider.get());
    }

    private MessageDao messageDao() {
      return DatabaseModule_ProvideMessageDaoFactory.provideMessageDao(provideDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>singletonMap("com.kchat.work.LocalCacheCleanupWorker", ((Provider) localCacheCleanupWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAccessTokenHolderProvider = DoubleCheck.provider(new SwitchingProvider<AccessTokenHolder>(singletonCImpl, 1));
      this.fakeTokenStoreProvider = DoubleCheck.provider(new SwitchingProvider<FakeTokenStore>(singletonCImpl, 3));
      this.dataStoreTokenStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStoreTokenStore>(singletonCImpl, 4));
      this.provideTokenStoreProvider = DoubleCheck.provider(new SwitchingProvider<TokenStore>(singletonCImpl, 2));
      this.fakeDeviceTokenStoreProvider = DoubleCheck.provider(new SwitchingProvider<FakeDeviceTokenStore>(singletonCImpl, 6));
      this.dataStoreDeviceTokenStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStoreDeviceTokenStore>(singletonCImpl, 7));
      this.provideDeviceTokenStoreProvider = DoubleCheck.provider(new SwitchingProvider<DeviceTokenStore>(singletonCImpl, 5));
      this.provideTokenAuthenticatorProvider = DoubleCheck.provider(new SwitchingProvider<TokenAuthenticator>(singletonCImpl, 8));
      this.provideImageLoaderProvider = DoubleCheck.provider(new SwitchingProvider<ImageLoader>(singletonCImpl, 0));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<KChatDatabase>(singletonCImpl, 11));
      this.chatLocalDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<ChatLocalDataSource>(singletonCImpl, 10));
      this.localCacheCleanupWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<LocalCacheCleanupWorker_AssistedFactory>(singletonCImpl, 9));
      this.providePushNavigationStoreProvider = DoubleCheck.provider(new SwitchingProvider<PushNavigationStore>(singletonCImpl, 12));
      this.pushNotificationHelperProvider = DoubleCheck.provider(new SwitchingProvider<PushNotificationHelper>(singletonCImpl, 13));
      this.provideCallSignalBusProvider = DoubleCheck.provider(new SwitchingProvider<CallSignalBus>(singletonCImpl, 16));
      this.fakeCallRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FakeCallRepository>(singletonCImpl, 15));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 20));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 19));
      this.provideKChatApiProvider = DoubleCheck.provider(new SwitchingProvider<KChatApi>(singletonCImpl, 18));
      this.networkCallRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NetworkCallRepository>(singletonCImpl, 17));
      this.provideCallRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CallRepository>(singletonCImpl, 14));
      this.fakeRealtimeCoordinatorProvider = DoubleCheck.provider(new SwitchingProvider<FakeRealtimeCoordinator>(singletonCImpl, 22));
      this.provideWsOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 25));
      this.provideWebSocketClientProvider = DoubleCheck.provider(new SwitchingProvider<KChatWebSocketClient>(singletonCImpl, 24));
      this.dataStoreEmergencyWipeStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStoreEmergencyWipeStore>(singletonCImpl, 29));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.provideEmergencyWipeStoreProvider = DoubleCheck.provider(new SwitchingProvider<EmergencyWipeStore>(singletonCImpl, 28));
      this.fakeChatRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FakeChatRepository>(singletonCImpl, 27));
      this.provideTypingStateStoreProvider = DoubleCheck.provider(new SwitchingProvider<TypingStateStore>(singletonCImpl, 31));
      this.provideApiBaseUrlProvider = DoubleCheck.provider(new SwitchingProvider<String>(singletonCImpl, 32));
      this.networkChatRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NetworkChatRepository>(singletonCImpl, 30));
      this.provideChatRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ChatRepository>(singletonCImpl, 26));
      this.fakeContactsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FakeContactsRepository>(singletonCImpl, 34));
      this.networkContactsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NetworkContactsRepository>(singletonCImpl, 35));
      this.provideContactsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ContactsRepository>(singletonCImpl, 33));
      this.provideActiveRoomTrackerProvider = DoubleCheck.provider(new SwitchingProvider<ActiveRoomTracker>(singletonCImpl, 36));
      this.noOpIncomingMessageNotifierProvider = DoubleCheck.provider(new SwitchingProvider<NoOpIncomingMessageNotifier>(singletonCImpl, 38));
      this.provideAppForegroundTrackerProvider = DoubleCheck.provider(new SwitchingProvider<AppForegroundTracker>(singletonCImpl, 40));
      this.workManagerLocalCacheCleanupSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<WorkManagerLocalCacheCleanupScheduler>(singletonCImpl, 43));
      this.fakeSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FakeSettingsRepository>(singletonCImpl, 42));
      this.networkSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NetworkSettingsRepository>(singletonCImpl, 44));
      this.provideSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 41));
      this.incomingMessageNotifierImplProvider = DoubleCheck.provider(new SwitchingProvider<IncomingMessageNotifierImpl>(singletonCImpl, 39));
      this.provideIncomingMessageNotifierProvider = DoubleCheck.provider(new SwitchingProvider<IncomingMessageNotifier>(singletonCImpl, 37));
      this.networkRealtimeCoordinatorProvider = DoubleCheck.provider(new SwitchingProvider<NetworkRealtimeCoordinator>(singletonCImpl, 23));
      this.provideRealtimeCoordinatorProvider = DoubleCheck.provider(new SwitchingProvider<RealtimeCoordinator>(singletonCImpl, 21));
      this.fakePinLockStoreProvider = DoubleCheck.provider(new SwitchingProvider<FakePinLockStore>(singletonCImpl, 48));
      this.dataStorePinLockStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStorePinLockStore>(singletonCImpl, 49));
      this.providePinLockStoreProvider = DoubleCheck.provider(new SwitchingProvider<PinLockStore>(singletonCImpl, 47));
      this.fakeAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FakeAuthRepository>(singletonCImpl, 46));
      this.networkAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NetworkAuthRepository>(singletonCImpl, 50));
    }

    @SuppressWarnings("unchecked")
    private void initialize3(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 45));
      this.fakePushTokenRegistrarProvider = DoubleCheck.provider(new SwitchingProvider<FakePushTokenRegistrar>(singletonCImpl, 56));
      this.networkPushTokenRegistrarProvider = DoubleCheck.provider(new SwitchingProvider<NetworkPushTokenRegistrar>(singletonCImpl, 57));
      this.providePushTokenRegistrarProvider = DoubleCheck.provider(new SwitchingProvider<PushTokenRegistrar>(singletonCImpl, 55));
      this.fcmTokenHandlerProvider = DoubleCheck.provider(new SwitchingProvider<FcmTokenHandler>(singletonCImpl, 54));
      this.firebasePushTokenSyncProvider = DoubleCheck.provider(new SwitchingProvider<FirebasePushTokenSync>(singletonCImpl, 53));
      this.noOpPushTokenSyncProvider = DoubleCheck.provider(new SwitchingProvider<NoOpPushTokenSync>(singletonCImpl, 58));
      this.providePushTokenSyncProvider = DoubleCheck.provider(new SwitchingProvider<PushTokenSync>(singletonCImpl, 52));
      this.provideSessionCoordinatorProvider = DoubleCheck.provider(new SwitchingProvider<SessionCoordinator>(singletonCImpl, 51));
      this.emergencyWipeCoordinatorImplProvider = DoubleCheck.provider(new SwitchingProvider<EmergencyWipeCoordinatorImpl>(singletonCImpl, 60));
      this.provideEmergencyWipeCoordinatorProvider = DoubleCheck.provider(new SwitchingProvider<EmergencyWipeCoordinator>(singletonCImpl, 59));
    }

    @Override
    public void injectKChatApplication(KChatApplication arg0) {
      injectKChatApplication2(arg0);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private KChatApplication injectKChatApplication2(KChatApplication instance) {
      KChatApplication_MembersInjector.injectImageLoader(instance, provideImageLoaderProvider.get());
      KChatApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // coil.ImageLoader 
          return (T) NetworkModule_ProvideImageLoaderFactory.provideImageLoader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAccessTokenHolderProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideDeviceTokenStoreProvider.get(), singletonCImpl.provideTokenAuthenticatorProvider.get());

          case 1: // com.kchat.data.repository.AccessTokenHolder 
          return (T) RepositoryModule_ProvideAccessTokenHolderFactory.provideAccessTokenHolder();

          case 2: // com.kchat.data.repository.TokenStore 
          return (T) RepositoryModule_ProvideTokenStoreFactory.provideTokenStore(singletonCImpl.fakeTokenStoreProvider.get(), singletonCImpl.dataStoreTokenStoreProvider.get());

          case 3: // com.kchat.data.fake.FakeTokenStore 
          return (T) new FakeTokenStore(singletonCImpl.provideAccessTokenHolderProvider.get());

          case 4: // com.kchat.data.network.auth.DataStoreTokenStore 
          return (T) new DataStoreTokenStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAccessTokenHolderProvider.get());

          case 5: // com.kchat.data.repository.DeviceTokenStore 
          return (T) RepositoryModule_ProvideDeviceTokenStoreFactory.provideDeviceTokenStore(singletonCImpl.fakeDeviceTokenStoreProvider.get(), singletonCImpl.dataStoreDeviceTokenStoreProvider.get());

          case 6: // com.kchat.data.fake.FakeDeviceTokenStore 
          return (T) new FakeDeviceTokenStore();

          case 7: // com.kchat.data.network.auth.DataStoreDeviceTokenStore 
          return (T) new DataStoreDeviceTokenStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.kchat.data.network.auth.TokenAuthenticator 
          return (T) NetworkModule_ProvideTokenAuthenticatorFactory.provideTokenAuthenticator(singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideAccessTokenHolderProvider.get(), singletonCImpl.provideDeviceTokenStoreProvider.get());

          case 9: // com.kchat.work.LocalCacheCleanupWorker_AssistedFactory 
          return (T) new LocalCacheCleanupWorker_AssistedFactory() {
            @Override
            public LocalCacheCleanupWorker create(Context context, WorkerParameters params) {
              return new LocalCacheCleanupWorker(context, params, singletonCImpl.chatLocalDataSourceProvider.get());
            }
          };

          case 10: // com.kchat.data.local.ChatLocalDataSource 
          return (T) new ChatLocalDataSource(singletonCImpl.roomDao(), singletonCImpl.messageDao());

          case 11: // com.kchat.data.local.KChatDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.kchat.data.repository.PushNavigationStore 
          return (T) RepositoryModule_ProvidePushNavigationStoreFactory.providePushNavigationStore();

          case 13: // com.kchat.push.PushNotificationHelper 
          return (T) new PushNotificationHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.providePushNavigationStoreProvider.get());

          case 14: // com.kchat.data.repository.CallRepository 
          return (T) RepositoryModule_ProvideCallRepositoryFactory.provideCallRepository(singletonCImpl.fakeCallRepositoryProvider.get(), singletonCImpl.networkCallRepositoryProvider.get());

          case 15: // com.kchat.data.fake.FakeCallRepository 
          return (T) new FakeCallRepository(singletonCImpl.provideCallSignalBusProvider.get());

          case 16: // com.kchat.data.repository.CallSignalBus 
          return (T) RepositoryModule_ProvideCallSignalBusFactory.provideCallSignalBus();

          case 17: // com.kchat.data.network.repository.NetworkCallRepository 
          return (T) new NetworkCallRepository(singletonCImpl.provideKChatApiProvider.get());

          case 18: // com.kchat.data.network.api.KChatApi 
          return (T) NetworkModule_ProvideKChatApiFactory.provideKChatApi(singletonCImpl.provideRetrofitProvider.get());

          case 19: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 20: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.provideAccessTokenHolderProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideDeviceTokenStoreProvider.get(), singletonCImpl.provideTokenAuthenticatorProvider.get());

          case 21: // com.kchat.data.repository.RealtimeCoordinator 
          return (T) RepositoryModule_ProvideRealtimeCoordinatorFactory.provideRealtimeCoordinator(singletonCImpl.fakeRealtimeCoordinatorProvider.get(), singletonCImpl.networkRealtimeCoordinatorProvider.get());

          case 22: // com.kchat.data.fake.FakeRealtimeCoordinator 
          return (T) new FakeRealtimeCoordinator();

          case 23: // com.kchat.data.network.repository.NetworkRealtimeCoordinator 
          return (T) new NetworkRealtimeCoordinator(singletonCImpl.provideWebSocketClientProvider.get(), singletonCImpl.provideKChatApiProvider.get(), singletonCImpl.provideAccessTokenHolderProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideDeviceTokenStoreProvider.get(), singletonCImpl.chatLocalDataSourceProvider.get(), singletonCImpl.provideChatRepositoryProvider.get(), singletonCImpl.provideContactsRepositoryProvider.get(), singletonCImpl.provideActiveRoomTrackerProvider.get(), singletonCImpl.provideTypingStateStoreProvider.get(), singletonCImpl.provideCallSignalBusProvider.get(), singletonCImpl.provideCallRepositoryProvider.get(), singletonCImpl.provideIncomingMessageNotifierProvider.get(), singletonCImpl.provideApiBaseUrlProvider.get());

          case 24: // com.kchat.data.network.ws.KChatWebSocketClient 
          return (T) NetworkModule_ProvideWebSocketClientFactory.provideWebSocketClient(singletonCImpl.provideWsOkHttpClientProvider.get(), singletonCImpl.provideAccessTokenHolderProvider.get());

          case 25: // @javax.inject.Named("ws") okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideWsOkHttpClientFactory.provideWsOkHttpClient();

          case 26: // com.kchat.data.repository.ChatRepository 
          return (T) RepositoryModule_ProvideChatRepositoryFactory.provideChatRepository(singletonCImpl.fakeChatRepositoryProvider.get(), singletonCImpl.networkChatRepositoryProvider.get());

          case 27: // com.kchat.data.fake.FakeChatRepository 
          return (T) new FakeChatRepository(singletonCImpl.provideEmergencyWipeStoreProvider.get());

          case 28: // com.kchat.data.repository.EmergencyWipeStore 
          return (T) RepositoryModule_ProvideEmergencyWipeStoreFactory.provideEmergencyWipeStore(singletonCImpl.dataStoreEmergencyWipeStoreProvider.get());

          case 29: // com.kchat.data.network.emergency.DataStoreEmergencyWipeStore 
          return (T) new DataStoreEmergencyWipeStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 30: // com.kchat.data.network.repository.NetworkChatRepository 
          return (T) new NetworkChatRepository(singletonCImpl.provideKChatApiProvider.get(), singletonCImpl.chatLocalDataSourceProvider.get(), singletonCImpl.provideEmergencyWipeStoreProvider.get(), singletonCImpl.provideTypingStateStoreProvider.get(), singletonCImpl.provideOkHttpClientProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideApiBaseUrlProvider.get());

          case 31: // com.kchat.data.repository.TypingStateStore 
          return (T) RepositoryModule_ProvideTypingStateStoreFactory.provideTypingStateStore();

          case 32: // @javax.inject.Named("apiBaseUrl") java.lang.String 
          return (T) NetworkModule_ProvideApiBaseUrlFactory.provideApiBaseUrl();

          case 33: // com.kchat.data.repository.ContactsRepository 
          return (T) RepositoryModule_ProvideContactsRepositoryFactory.provideContactsRepository(singletonCImpl.fakeContactsRepositoryProvider.get(), singletonCImpl.networkContactsRepositoryProvider.get());

          case 34: // com.kchat.data.fake.FakeContactsRepository 
          return (T) new FakeContactsRepository();

          case 35: // com.kchat.data.network.repository.NetworkContactsRepository 
          return (T) new NetworkContactsRepository(singletonCImpl.provideKChatApiProvider.get(), singletonCImpl.provideEmergencyWipeStoreProvider.get(), singletonCImpl.provideApiBaseUrlProvider.get());

          case 36: // com.kchat.data.repository.ActiveRoomTracker 
          return (T) RepositoryModule_ProvideActiveRoomTrackerFactory.provideActiveRoomTracker();

          case 37: // com.kchat.data.repository.IncomingMessageNotifier 
          return (T) RepositoryModule_ProvideIncomingMessageNotifierFactory.provideIncomingMessageNotifier(singletonCImpl.noOpIncomingMessageNotifierProvider.get(), singletonCImpl.incomingMessageNotifierImplProvider.get());

          case 38: // com.kchat.data.fake.NoOpIncomingMessageNotifier 
          return (T) new NoOpIncomingMessageNotifier();

          case 39: // com.kchat.push.IncomingMessageNotifierImpl 
          return (T) new IncomingMessageNotifierImpl(singletonCImpl.pushNotificationHelperProvider.get(), singletonCImpl.provideAppForegroundTrackerProvider.get(), singletonCImpl.provideActiveRoomTrackerProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get());

          case 40: // com.kchat.data.repository.AppForegroundTracker 
          return (T) RepositoryModule_ProvideAppForegroundTrackerFactory.provideAppForegroundTracker();

          case 41: // com.kchat.data.repository.SettingsRepository 
          return (T) RepositoryModule_ProvideSettingsRepositoryFactory.provideSettingsRepository(singletonCImpl.fakeSettingsRepositoryProvider.get(), singletonCImpl.networkSettingsRepositoryProvider.get());

          case 42: // com.kchat.data.fake.FakeSettingsRepository 
          return (T) new FakeSettingsRepository(singletonCImpl.workManagerLocalCacheCleanupSchedulerProvider.get());

          case 43: // com.kchat.work.WorkManagerLocalCacheCleanupScheduler 
          return (T) new WorkManagerLocalCacheCleanupScheduler(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 44: // com.kchat.data.network.repository.NetworkSettingsRepository 
          return (T) new NetworkSettingsRepository(singletonCImpl.provideKChatApiProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideDeviceTokenStoreProvider.get(), singletonCImpl.workManagerLocalCacheCleanupSchedulerProvider.get(), singletonCImpl.provideApiBaseUrlProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 45: // com.kchat.data.repository.AuthRepository 
          return (T) RepositoryModule_ProvideAuthRepositoryFactory.provideAuthRepository(singletonCImpl.fakeAuthRepositoryProvider.get(), singletonCImpl.networkAuthRepositoryProvider.get());

          case 46: // com.kchat.data.fake.FakeAuthRepository 
          return (T) new FakeAuthRepository(singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get(), singletonCImpl.providePinLockStoreProvider.get(), singletonCImpl.provideContactsRepositoryProvider.get(), singletonCImpl.provideEmergencyWipeStoreProvider.get());

          case 47: // com.kchat.data.repository.PinLockStore 
          return (T) RepositoryModule_ProvidePinLockStoreFactory.providePinLockStore(singletonCImpl.fakePinLockStoreProvider.get(), singletonCImpl.dataStorePinLockStoreProvider.get());

          case 48: // com.kchat.data.fake.FakePinLockStore 
          return (T) new FakePinLockStore();

          case 49: // com.kchat.data.network.auth.DataStorePinLockStore 
          return (T) new DataStorePinLockStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 50: // com.kchat.data.network.repository.NetworkAuthRepository 
          return (T) new NetworkAuthRepository(singletonCImpl.provideKChatApiProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.chatLocalDataSourceProvider.get(), singletonCImpl.provideRealtimeCoordinatorProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get(), singletonCImpl.providePinLockStoreProvider.get(), singletonCImpl.provideContactsRepositoryProvider.get(), singletonCImpl.provideEmergencyWipeStoreProvider.get());

          case 51: // com.kchat.data.repository.SessionCoordinator 
          return (T) RepositoryModule_ProvideSessionCoordinatorFactory.provideSessionCoordinator(singletonCImpl.provideChatRepositoryProvider.get(), singletonCImpl.provideContactsRepositoryProvider.get(), singletonCImpl.provideRealtimeCoordinatorProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get(), singletonCImpl.workManagerLocalCacheCleanupSchedulerProvider.get(), singletonCImpl.providePushTokenSyncProvider.get(), singletonCImpl.fcmTokenHandlerProvider.get());

          case 52: // com.kchat.data.repository.PushTokenSync 
          return (T) RepositoryModule_ProvidePushTokenSyncFactory.providePushTokenSync(singletonCImpl.firebasePushTokenSyncProvider.get(), singletonCImpl.noOpPushTokenSyncProvider.get());

          case 53: // com.kchat.push.FirebasePushTokenSync 
          return (T) new FirebasePushTokenSync(singletonCImpl.fcmTokenHandlerProvider.get());

          case 54: // com.kchat.push.FcmTokenHandler 
          return (T) new FcmTokenHandler(singletonCImpl.providePushTokenRegistrarProvider.get(), singletonCImpl.provideTokenStoreProvider.get(), singletonCImpl.provideDeviceTokenStoreProvider.get());

          case 55: // com.kchat.data.repository.PushTokenRegistrar 
          return (T) RepositoryModule_ProvidePushTokenRegistrarFactory.providePushTokenRegistrar(singletonCImpl.fakePushTokenRegistrarProvider.get(), singletonCImpl.networkPushTokenRegistrarProvider.get());

          case 56: // com.kchat.data.fake.FakePushTokenRegistrar 
          return (T) new FakePushTokenRegistrar();

          case 57: // com.kchat.data.network.repository.NetworkPushTokenRegistrar 
          return (T) new NetworkPushTokenRegistrar(singletonCImpl.provideKChatApiProvider.get());

          case 58: // com.kchat.data.fake.NoOpPushTokenSync 
          return (T) new NoOpPushTokenSync();

          case 59: // com.kchat.data.repository.EmergencyWipeCoordinator 
          return (T) RepositoryModule_ProvideEmergencyWipeCoordinatorFactory.provideEmergencyWipeCoordinator(singletonCImpl.emergencyWipeCoordinatorImplProvider.get());

          case 60: // com.kchat.data.network.emergency.EmergencyWipeCoordinatorImpl 
          return (T) new EmergencyWipeCoordinatorImpl(singletonCImpl.provideChatRepositoryProvider.get(), singletonCImpl.provideContactsRepositoryProvider.get(), singletonCImpl.provideEmergencyWipeStoreProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
