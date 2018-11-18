package com.codylab.foodie.core.paging

import android.arch.paging.PositionalDataSource
import com.codylab.foodie.core.model.Location
import com.codylab.foodie.core.repository.UserLocationRepository
import com.codylab.foodie.core.repository.ZomatoRestaurantRepository
import com.codylab.foodie.core.zomato.model.search.Restaurant
import io.reactivex.subjects.BehaviorSubject

class RestaurantDataSource(
    private val restaurantRepository: ZomatoRestaurantRepository,
    private val userLocationRepository: UserLocationRepository,
    private var location: Location? = null
) : PositionalDataSource<Restaurant>() {

    val networkStateSubject = BehaviorSubject.create<NetworkState>()

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Restaurant>) {
        networkStateSubject.onNext(NetworkState.LOADING)

        try {
            if (location == null) {
                this.location = userLocationRepository.getLocationUpdates().firstOrError().blockingGet()
            }

            val response =
                restaurantRepository.getRestaurantsResponse(location!!, params.requestedStartPosition, params.pageSize)
                    .blockingGet()
            networkStateSubject.onNext(NetworkState.LOADED)
            val restaurants = response.restaurants.map { it.restaurant }
            callback.onResult(restaurants, response.results_start)
        } catch (e: NoSuchElementException) {
            networkStateSubject.onNext(NetworkState.error(e.toString()))
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Restaurant>) {
        networkStateSubject.onNext(NetworkState.LOADING)
        try {
            val response =
                restaurantRepository.getRestaurantsResponse(location!!, params.startPosition, params.loadSize)
                    .blockingGet()
            networkStateSubject.onNext(NetworkState.LOADED)
            val restaurants = response.restaurants.map { it.restaurant }
            callback.onResult(restaurants)
        } catch (e: NoSuchElementException) {
            networkStateSubject.onNext(NetworkState.error(e.toString()))
        }
    }
}