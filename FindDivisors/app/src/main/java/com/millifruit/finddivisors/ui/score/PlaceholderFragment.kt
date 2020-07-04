package com.millifruit.finddivisors.ui.score

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.millifruit.finddivisors.R
import com.millifruit.finddivisors.myConstants.GMConstants
import com.millifruit.finddivisors.myRealmDB.HighScoreTable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort



/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {
    private lateinit var pageViewModel: PageViewModel
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var realmResults : RealmResults<HighScoreTable>
    private lateinit var adapter: ScoreRecyclerViewAdapter
    private val TAG: String = "getHSDB"
    private lateinit var realm: Realm

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
            Log.d(
                TAG,
                "onCreate arguments?.getInt(ARG_SECTION_NUMBER) : ${arguments?.getInt(
                    ARG_SECTION_NUMBER
                )}"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_score, container, false)
        val textView: TextView = root.findViewById(R.id.section_label)
        val scoreRecyclerView: RecyclerView = root.findViewById(R.id.score_recycler_view)
        val jsonTextView: TextView = root.findViewById(R.id.json_result_text_view)

        realm = Realm.getDefaultInstance()


        layoutManager = LinearLayoutManager(this.context)//어떤 레이아웃으로 보여줄건지 설정
//        layoutManager = LinearLayoutManager(this)로 하면 못알아 먹기 때문에 this.context로 한다.
        scoreRecyclerView.layoutManager = layoutManager//어떤 xml뷰에 위에 선언한 것을 적용할 것인지 설정
        pageViewModel.gameMode.observe(viewLifecycleOwner, Observer<Int> {
            //it은 1,2,3으로 순환한다. realmResult에 각 게임모드별로 결과를 세팅함.
            when (it) {
                GMConstants.TEN_NUMBER_GAME.toInt() -> //DB결과를 재검색.
                    realmResults = realm.where(HighScoreTable::class.java)
                        .equalTo("gameMode", GMConstants.TEN_NUMBER_GAME.toInt()).findAll()
                        .sort("seconds", Sort.ASCENDING)//보여주고 싶은 DB결과를 선언

                GMConstants.HUNDRED_NUMBER_GAME.toInt() -> //DB결과를 재검색.
                    realmResults = realm.where(HighScoreTable::class.java)
                        .equalTo("gameMode", GMConstants.HUNDRED_NUMBER_GAME.toInt()).findAll()
                        .sort("seconds", Sort.ASCENDING)//보여주고 싶은 DB결과를 선언

                GMConstants.THOUSAND_NUMBER_GAME.toInt() -> //DB결과를 재검색.
                    realmResults = realm.where(HighScoreTable::class.java)
                        .equalTo("gameMode", GMConstants.THOUSAND_NUMBER_GAME.toInt()).findAll()
                        .sort("seconds", Sort.ASCENDING)//보여주고 싶은 DB결과를 선언
            }




            adapter = context?.let { it1 -> ScoreRecyclerViewAdapter(it1, realmResults) }!!
            scoreRecyclerView.adapter = adapter //위 어댑터를 리사이클러xml뷰에 설정.(여기 까지 하면 나옴.)

        })




        //리사이클러뷰에 구분선 추가.
        scoreRecyclerView.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))

        // this를 viewLifecycleOwner로 수정헀음.
        pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
            textView.text = it
            jsonTextView.text = it
            Log.d(TAG, "textView.text : $it")
        })

        Log.d(TAG,"onCreateView arguments?.getInt(ARG_SECTION_NUMBER) : ${arguments?.getInt(
            ARG_SECTION_NUMBER
        )}")

        return root
    }
}

