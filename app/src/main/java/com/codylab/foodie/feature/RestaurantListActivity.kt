package com.codylab.foodie.feature

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import androidx.core.view.isVisible
import com.codylab.foodie.R
import com.codylab.foodie.core.BaseActivity
import com.codylab.foodie.core.extension.*
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_restaurant_list.*
import javax.inject.Inject

class RestaurantListActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: RestaurantListViewModel

    private lateinit var restaurantAdapter: RestaurantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        restaurantAdapter = RestaurantListAdapter()
        restaurantList.adapter = restaurantAdapter
        restaurantList.layoutManager = LinearLayoutManager(this)

        viewModel = getViewModel(viewModelFactory)

        viewModel.uiModelLiveData.nonNull().observeNonNull(this) { uiModel ->
            uiModel.message?.getDataIfNotHandled()?.let {
                showToast(it)
            }

            uiModel.zomatoRestaurantList?.let { pagedRestaurants ->
                restaurantAdapter.submitList(pagedRestaurants)
            }

            uiModel.isLoading.let { isLoading ->
                progressBar.isVisible = isLoading
            }
        }

        swiperefresh.setOnRefreshListener {
            swiperefresh.isRefreshing = false
            viewModel.onRefresh()
        }
    }

    override fun onStart() {
        super.onStart()

        disposables += requestLocationPermission().subscribe({ granted ->
            viewModel.onLocationRequested(granted)
        }, {
            showError(it)
        })
    }
}
