package com.millifruit.finddivisors.ui.score

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.millifruit.finddivisors.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1_easy,
    R.string.tab_text_2_medium,
    R.string.tab_text_3_hard
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {


        //이말 즉슨,,,  프래그먼트를 리사이클러뷰의 아이템처럼 불러낸다는 것으로 이해하면 될 것 같다.
        //PlaceHolderFragment를 뉴인스턴트를 불러오는 것이다. 어떤 포지션의? 0,1,2의 포지션에 있는...()

        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        //각 아이템의 타이틀을 받아온다.
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 3 total pages.
        //뷰페이저에 몇개를 보여줄 것인지 설정. easy,medium,hard니까 3개로 돌림.
        return TAB_TITLES.size
    }
}