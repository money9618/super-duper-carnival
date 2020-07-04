package com.millifruit.finddivisors

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.millifruit.finddivisors.myConstants.GMConstants
import com.millifruit.finddivisors.myConstants.IntentNameConstants
import com.millifruit.finddivisors.myRealmDB.LastFoundNumberTable
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private var realmResult: LastFoundNumberTable? = null
    private val TAG: String = "getHSDB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //초기화....
        realm = Realm.getDefaultInstance()
        realmResult = realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
            .findFirst()//보여주고 싶은 DB결과를 선언


        continue_text_view.setOnClickListener {


            if (realmResult != null) {
                //...게임을 실행한다.(인텐트에 숫자를 불러와서 저장한고 다음 엑티비티에 넘긴다.)
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("lastFoundNumber", realmResult!!.lastFoundNumber)
                intent.putExtra("second",realmResult!!.seconds)
                intent.putExtra(IntentNameConstants.GAME_IDENTIFY_CODE, GMConstants.CONTINUE)
                startActivity(intent)
                Log.d(TAG,"load game")
                //Toast.makeText(this, "load game", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG,"New Game을 실행하여 초기 데이터를 만들어 주세요")
     //           Toast.makeText(this, "New Game을 실행하여 초기 데이터를 만들어 주세요.", Toast.LENGTH_SHORT).show()
           }
        }

        new_game_text_view.setOnClickListener {
            //maxId를 찾는다.
            val maxId = realm.where<LastFoundNumberTable>().max("id")
            val intent = Intent(this, NewGameSelectActivity::class.java)
            startActivity(intent)

        }

        //스코어보드 클릭시...
        score_board_text_view.setOnClickListener {
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)

        }

        setting_text_view.setOnClickListener {
    //        Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show()
        }

        info_text_view.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("menu", "Info")
            startActivity(intent)
    //        Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show()
        }


        db_delete_button.setOnClickListener {
            realm.executeTransaction {
                realm = Realm.getDefaultInstance()
                realm.where(LastFoundNumberTable::class.java).findAll().deleteAllFromRealm()
            }
            realmResult = realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
                .findFirst()//보여주고 싶은 DB결과를 선언
     //       Toast.makeText(this, "deleted DB", Toast.LENGTH_SHORT).show()
        }


       }








    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        realmResult = realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
            .findFirst()//보여주고 싶은 DB결과를 선언

        //세이브파일이 없을 때...continue 버튼을 숨긴다.
        //if (realmResult == null) {
      //      //...버튼을 숨긴다.
      //      continue_text_view.visibility = View.GONE
      //  } //세이브파일이 1이상 있을 때...continue버튼을 부인다.
      //  else {
      //      //...버튼을 보인다.
      //      continue_text_view.visibility = View.VISIBLE
      //  }

        //다른 액티비티 갔다가 다시 돌아왔을 때 realm 인스턴트를 재생성해서 넣어준다.
    }

}