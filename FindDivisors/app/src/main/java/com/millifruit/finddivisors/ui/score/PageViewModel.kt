package com.millifruit.finddivisors.ui.score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.millifruit.finddivisors.myConstants.GMConstants

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()
    fun setIndex(index: Int) {
        _index.value = index
    }

    val text: LiveData<String> = Transformations.map(_index) {
        "Hello world from section: $it"
    }

    val gameMode: LiveData<Int> = Transformations.map(_index) {
        when (it) {
            1 -> GMConstants.TEN_NUMBER_GAME.toInt()
            2 -> GMConstants.HUNDRED_NUMBER_GAME.toInt()
            3 -> GMConstants.THOUSAND_NUMBER_GAME.toInt()
            else -> 0
        }
    }


    //   }
}