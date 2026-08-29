package com.kchat.push;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;
import javax.annotation.processing.Generated;

@OriginatingElement(
    topLevelClass = KChatFirebaseMessagingService.class
)
@GeneratedEntryPoint
@InstallIn(ServiceComponent.class)
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.InjectorEntryPointGenerator")
public interface KChatFirebaseMessagingService_GeneratedInjector {
  void injectKChatFirebaseMessagingService(
      KChatFirebaseMessagingService kChatFirebaseMessagingService);
}
