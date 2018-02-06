package com.makaroffandrey.examplecafe.repo

sealed class Resource<T>
class LoadingResource<T> : Resource<T>()
data class FinishedResource<T>(val data: T) : Resource<T>()
data class ErrorResource<T>(val throwable: Throwable) : Resource<T>()