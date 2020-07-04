package com.millifruit.finddivisors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.millifruit.finddivisors.myConstants.GMConstants
import kotlinx.android.synthetic.main.activity_new_game_select.*

class NewGameSelectActivity : AppCompatActivity() {

    private lateinit var adapter: GameModeRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    //게임 항목은 여기서 관리.
    private var gameModeList = arrayListOf<GameMode>(
        //GameMode("New Game", "1000까지 나눠보세요.", "1~1000",null, GMConstants.NEW_GAME.toInt()),//new game
        GameMode("Easy", "100이하의 숫자 10개를 나눠보세요", "10~100",null, GMConstants.TEN_NUMBER_GAME.toInt()),//1
        GameMode("Medium", "1000이하의 숫자 10개를 나눠보세요.","100~1000", null, GMConstants.HUNDRED_NUMBER_GAME.toInt()),//10
        GameMode("Hard", "10000이하의 숫자 10개를 나눠보세요.", "1000~10000",null, GMConstants.THOUSAND_NUMBER_GAME.toInt()),//100
        GameMode("Mathematician", "무한 나누기에 도전하세요.", "1~100000000",null, GMConstants.INFINITY_GAME.toInt())//infinity game
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game_select)
        //gameModeList 어레이리스트를 생성.


        layoutManager = LinearLayoutManager(this)//어떤 레이아웃으로 보여줄건지 설정
//        layoutManager = LinearLayoutManager(this)로 하면 못알아 먹기 때문에 this.context로 한다.
        new_game_recycler_view.layoutManager = layoutManager//어떤 xml뷰에 위에 선언한 것을 적용할 것인지 설정
        adapter =
            GameModeRecyclerViewAdapter(this, gameModeList)// 어뎁터에다가 DB결과를 커스텀어댑터에 집어넣고, 그것을 어댑터에 설정
        new_game_recycler_view.adapter = this.adapter //위 어댑터를 리사이클러xml뷰에 설정.(여기 까지 하면 나옴.)
    }


    //엑티비티가 눈에 안보일떄...
    override fun onStop() {
        super.onStop()
        finish()
    }

    //엑티비티가 끝났을 때...
    override fun onDestroy() {
        super.onDestroy()
    }
}