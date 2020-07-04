package com.millifruit.finddivisors

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.millifruit.finddivisors.myConstants.TAG
import com.millifruit.finddivisors.myConstants.GMConstants
import com.millifruit.finddivisors.myConstants.IntentNameConstants
import com.millifruit.finddivisors.myRealmDB.LastFoundNumberTable
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*


class AfterClickGameItemActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //초기화....
        realm = Realm.getDefaultInstance()
        realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
            .findFirst()//보여주고 싶은 DB결과를 선언

        //intent에서 게임코드를 불러와 변수에 저장한다.
        val gameIdentifyCode = intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)?.toInt()

        //게임선택에서 실행까지...여러개의 로그 중 하나.
        Log.d(
          TAG.BGA,
            "AfterCGIActivity에서.... 인텐트 내용 : ${intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)
                ?.toInt()}"
        )


        //코드를 받아서 게임을 실행한다.
        when (gameIdentifyCode) {
            //'new Game'item이 선택됬을 때...
            GMConstants.NEW_GAME.toInt() -> {
                //maxId를 찾는다.

                val maxId = realm.where<LastFoundNumberTable>().max("id")
                //같이 없으면?...
                if (maxId?.toLong() == null) {
                    //...DB에 세이브 기록을 추가한다.
                    realm.executeTransaction {
                        val nextId = (maxId?.toLong() ?: 0L) + 1L//세이브파일이 늘어날 때를 대비해서 이대로 냅둠.
                        val lastFoundNumber = realm.createObject<LastFoundNumberTable>(nextId)
                        lastFoundNumber.lastFoundNumber = 2
                        lastFoundNumber.entryDate = Date()//최초등록일자
                        lastFoundNumber.updateDate = Date()//최초등록이기 때문에 entryDate와 같은 값 대입.
                    }
                    val intent = Intent(applicationContext, GameActivity::class.java)
                    intent.putExtra(IntentNameConstants.GAME_IDENTIFY_CODE, GMConstants.NEW_GAME)
                    startActivity(intent)
                    //Toast.makeText( this,"new game. nextId:${realm.where<LastFoundNumberTable>().equalTo("id", 1L).findFirst()?.id},", Toast.LENGTH_SHORT).show()

                } //아이디가 이미 존재한다면... 세이브 데이터를 늘릴지 물어본다.
                else {
                    Toast.makeText(this, "savedata is already existed", Toast.LENGTH_SHORT).show()

                    //세이브데이터를 만들어서


                    //NewGameSelectActivity를 실행한다.
                    //val intent = Intent(this, NewGameSelectActivity::class.java)
                    //intent.putExtra("menu","Infinity Game")
                    //startActivity(intent)
                }
            }
            //Infinity Game을 클릭받았을 때...
            GMConstants.INFINITY_GAME.toInt() -> {
                val intent = Intent(applicationContext, GameActivity::class.java)
                intent.putExtra(IntentNameConstants.GAME_IDENTIFY_CODE, GMConstants.INFINITY_GAME)
                startActivity(intent)
                //Toast.makeText(this, "infinity : 에프터클릭게임아이템엑티비티", Toast.LENGTH_SHORT).show()
            }
            GMConstants.HUNDRED_NUMBER_GAME.toInt() -> {val intent = Intent(applicationContext, GameActivity::class.java)
                intent.putExtra(IntentNameConstants.GAME_IDENTIFY_CODE, GMConstants.HUNDRED_NUMBER_GAME)
                startActivity(intent)
                //Toast.makeText(this, "hundred", Toast.LENGTH_SHORT).show()

                //
            }
            GMConstants.TEN_NUMBER_GAME.toInt() -> {val intent = Intent(applicationContext, GameActivity::class.java)
                intent.putExtra(IntentNameConstants.GAME_IDENTIFY_CODE, GMConstants.TEN_NUMBER_GAME)
                startActivity(intent)
                //Toast.makeText(this, "ten", Toast.LENGTH_SHORT).show()

                //
            }
            GMConstants.THOUSAND_NUMBER_GAME.toInt() -> {val intent = Intent(applicationContext, GameActivity::class.java)
                intent.putExtra(IntentNameConstants.GAME_IDENTIFY_CODE, GMConstants.THOUSAND_NUMBER_GAME)
                startActivity(intent)
                //Toast.makeText(this, "thou", Toast.LENGTH_SHORT).show()


            }

            //게임 코드가 없을경우...
            else -> {
                //Toast.makeText(this, "존재하지 않는 게임입니다.", Toast.LENGTH_SHORT).show()
                Log.d(
                    TAG.BGA,
                    "왜 이게 뜰까${intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)}")
            }

        }

        //액티비티를 종료...기독교에서 기도끝났을 때 항상 하는 amen같은 느낌임.
        finish()
    }


}