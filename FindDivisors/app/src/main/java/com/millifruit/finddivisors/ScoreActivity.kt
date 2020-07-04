package com.millifruit.finddivisors

import GetHighScoresDBData
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.millifruit.finddivisors.myRealmDB.HighScoreTable
import com.millifruit.finddivisors.ui.score.SectionsPagerAdapter
import io.realm.Realm


class ScoreActivity : AppCompatActivity() {
    private val TAG : String = "getHSDB"
    private lateinit var realm: Realm
    private val waitMilliSeconds : Long = 5000

    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        deleteRealmDB()
        Log.d(TAG, "onCreate : DB삭제")
        //서버DB 불러오기.
        //getMysqlDBDataFromJson()
        getMysqlDBTestDataFromJson()

        loading()//로딩 표시
        val delayHandler = Handler()
        delayHandler.postDelayed(Runnable {

            //상단 탭을 설정하는 선언. 어댑터 지정해주고,
            val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
            //activity_score의 리사이클러뷰가 나올 뷰를 선언해주고, 뷰패이저의 어댑텅에 세션페이저 어댑터를 넣어준다.
            val viewPager: ViewPager = findViewById(R.id.view_pager)
            viewPager.adapter = sectionsPagerAdapter
            //val scoreRecyclerView : RecyclerView = findViewById(R.id.score_recycler_view)
            //상단의 탭의 레이아웃르 선언해준다.
            val tabs: TabLayout = findViewById(R.id.tabs)
            tabs.setupWithViewPager(viewPager)

            loadingEnd()//로딩 표시 끝.
        }, waitMilliSeconds)
    }


    private fun loading() {
        //로딩
        Handler().postDelayed(
            {
                progressDialog = ProgressDialog(this)
                progressDialog!!.isIndeterminate = true
                progressDialog!!.setMessage("road data...please wait...")
                progressDialog!!.show()
            }, 0
        )
    }

    private fun loadingEnd() {
        Handler().postDelayed(
            { progressDialog!!.dismiss() }, 0
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        //휴대폰 내부의 DB를 삭제한 후, 값을 넣어준 후, 표시.(삭제는 한번만 이루어 진다.)
        deleteRealmDB()
        Log.d(TAG, "onDestroy : DB삭제")
    }

    private fun getMysqlDBDataFromJson(){
          val getDBTask = GetHighScoresDBData()//겟하이스코어데이터 선언.
           Log.d(TAG,"before getDBTask : ${getDBTask.doneFlag}")
           getDBTask.execute("http://finddivisors.dothome.co.kr/getHighScoresJson.php")//php넣어서 실행하게 한다.
           Log.d(TAG,"after1 getDBTask : ${getDBTask.doneFlag}")
    }

    private fun getMysqlDBTestDataFromJson(){
        val getDBTask2 = GetHighScoresDBData()//겟하이스코어데이터 선언.
        Log.d(TAG,"before getDBTask2 : ${getDBTask2.doneFlag}")
        getDBTask2.execute("http://finddivisors.dothome.co.kr/getHighScoresTestJson.php")//php넣어서 실행하게 한다.
        Log.d(TAG,"after1 getDBTask2 : ${getDBTask2.doneFlag}")
    }

    private fun deleteRealmDB(){
        realm = Realm.getDefaultInstance()
        //삭제를 하려면 트랜잭션으로 해야한다.(추가,변경도 마찬가지.)
        realm.executeTransaction {
            realm.where(HighScoreTable::class.java).findAll().deleteAllFromRealm()
        }
    }

}

