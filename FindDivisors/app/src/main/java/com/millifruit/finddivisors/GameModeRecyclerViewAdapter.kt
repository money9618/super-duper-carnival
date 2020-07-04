package com.millifruit.finddivisors

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.millifruit.finddivisors.myConstants.IntentNameConstants

class GameModeRecyclerViewAdapter(
    private val context: Context,
    private val gameModeList: ArrayList<GameMode>
) :
    RecyclerView.Adapter<GameModeRecyclerViewAdapter.ViewHolder>() {

    //홀더를 설정. 이너홀더로 만들었다. 그리고 아이템뷰는 !!를 붙였는데 눌이 아닐것이라는 판단일 것이다. 아마도...
    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        private val gameModeName = itemView?.findViewById<TextView>(R.id.game_mode_name_text_view)
        private val gameModeDetails = itemView?.findViewById<TextView>(R.id.game_mode_details_text_view)
        private val gameModeImage = itemView?.findViewById<ImageView>(R.id.game_mode_image_view)
        private val gameModeNumberDetails = itemView?.findViewById<TextView>(R.id.number_details_text_view)

        fun bind(gameMode: GameMode, context: Context) {
            /* gameMode.photo의 setImageResource에 들어갈 이미지의 id를 파일명(String)으로 찾고,
            이미지가 없는 경우 안드로이드 기본 아이콘을 표시한다.*/
            if (gameMode.photo != null
            ) {
                val resourceId =
                    context.resources.getIdentifier(gameMode.photo, "drawable", context.packageName)
                gameModeImage?.setImageResource(resourceId)
            } else {
                gameModeImage?.setImageResource(R.mipmap.ic_launcher)
            }
            /* 나머지 TextView와 String 데이터를 연결한다. */
            gameModeName?.text = gameMode.Name
            gameModeDetails?.text = gameMode.details
            gameModeNumberDetails?.text= gameMode.numberDetails
        }

    }


    override fun getItemCount(): Int {
        return gameModeList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //var context = holder.itemView.context

        //받아온 홀더안에 있는 바인드를 실행하여준다.
        holder.bind(gameModeList[position], context)

        //받아온 홀더의 아이템 뷰에 클릭리스너를 설정하여 동작을 설정한다.
        holder.itemView.setOnClickListener {
            val gameIdCode = gameModeList[position].gameIdentifyCode.toString()

            //  Log.d("게임선택에서 실행까지","onBindViewHolder 게임이름 : ${gameModeList[position].Name}")
            // Log.d("게임선택에서 실행까지","onBindViewHolder 게임코드 : $gameIdCode")
            //Toast.makeText(it.context,"아이템 클릭 : ${gameModeList[position].Name}",Toast.LENGTH_SHORT).show()

            val intent = Intent(it.context, AfterClickGameItemActivity::class.java)
            intent.putExtra(
                IntentNameConstants.GAME_IDENTIFY_CODE,
                gameIdCode
            )
            //intent라는 이름의 Intent에 book_Id라고하는 아이디로 book_Id 값을 집어넣음.(늘림당한 아이템뷰의 bookId.)
            //Log.d("게임선택에서 실행까지", "ViewHolder클릭 전에 담긴 인텐트 내용 : ${intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)}")
            it.context.startActivity(intent)//intent를 실행한다.
            //게임선택에서 실행까지...여러개의 로그 중 하나.
            //Log.d("게임선택에서 실행까지","ViewHolder클릭 후에 담긴 인텐트 내용 : ${intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)}")
        }
    }

    // 화면을 최초 로딩하여 만들어진 View가 없는 경우, xml파일을 inflate하여 ViewHolder를 생성한다.
    // 그러니까, 레이아웃 파일ㅇ르 하나 찝어와서, 뷰를 담은 홀더를 반환하는 역할을 해준다.
    //홀더 하나하나가,
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.new_game_select_item, parent, false)
        return ViewHolder(view)
    }

}