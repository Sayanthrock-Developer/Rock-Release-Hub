package com.sayanthrock.rockreleasehub.core.network.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthNetworkModule {

    @Binds
    @Singleton
    abstract fun bindOAuthDeviceFlowGateway(
        implementation: GitHubOAuthDeviceFlowGateway
    ): OAuthDeviceFlowGateway

    @Binds
    @Singleton
    abstract fun bindAccessTokenStore(
        implementation: KeystoreAccessTokenStore
    ): AccessTokenStore
}
