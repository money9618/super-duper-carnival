package com.millifruit.finddivisors.ui.score

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.millifruit.finddivisors.R
import com.millifruit.finddivisors.myRealmDB.HighScoreTable
import io.realm.RealmResults
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class ScoreRecyclerViewAdapter(
    context: Context,
    realmResults: RealmResults<HighScoreTable>
) :
    RecyclerView.Adapter<ScoreRecyclerViewAdapter.ViewHolder>() {
    //데이터베이스를 불러온다.
    private val rResult: RealmResults<HighScoreTable> = realmResults
    private val mContext = context

    //홀더를 설정. 이너홀더로 만들었다. 그리고 아이템뷰는 !!를 붙였는데 눌이 아닐것이라는 판단일 것이다. 아마도...
    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        private val rankingTextView =
            itemView?.findViewById<TextView>(R.id.ranking_number_text_view)
        private val nameTextView = itemView?.findViewById<TextView>(R.id.name_text_view)
        private val dateTextView = itemView?.findViewById<TextView>(R.id.date_view)
        private val messageTextView = itemView?.findViewById<TextView>(R.id.message_text_view)
        private val secondsTextView = itemView?.findViewById<TextView>(R.id.seconds_text_view)
        private val countryImageView =
            itemView?.findViewById<ImageView>(R.id.country_flag_image_view)


        fun bind(highScoreTable: HighScoreTable) {

            /* 나머지 TextView와 String 데이터를 연결한다. */
            rankingTextView?.text = (position + 1).toString()//0,1,2부터시작하기 때문에 +1해준다.
            dateTextView?.text = SimpleDateFormat(
                "(yyyy/MM/dd)",
                Locale.getDefault()
            ).format(highScoreTable.entryDate)


            nameTextView?.text = highScoreTable.name
            messageTextView?.text = highScoreTable.message

            if (highScoreTable.name == "null") {
                nameTextView?.text = null
            }
            if (highScoreTable.message == "null") {
                messageTextView?.text = null
            }

            //게임이 실행된 국가를 국기로 표시.
            val countryCD = highScoreTable.country//국가코드를 받아옴.
            val assetManager: AssetManager = mContext.assets//
            var ipst: InputStream? = null
            try {
                ipst = assetManager.open("country_flags/${countryCD}.png")
                Log.d("국가", "${countryCD}.png")
                val bitmap = BitmapFactory.decodeStream(ipst)
                countryImageView?.setImageBitmap(bitmap)
                ipst.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //기록을 00:00:00:00로 변환해서 표시.
            val milliSeconds = highScoreTable.seconds
            val hour = milliSeconds / 360000 //한시간은 3600.00초니까...
            val minute =
                (milliSeconds % 360000) / 6000// 한시간단위는 다 나눠어 때버리고 나머지를 60으로 나눠서 값을 낸다.
            val sec = (milliSeconds / 100) % 60 // 1초가 지나면 time은 100이되는데 이 때 sec은 1이 됨
            val milli = milliSeconds % 100 // milli는 1~99까지만 출력되어야 하니 100으로 나눈 나머지임

            if (milliSeconds < 6000) {
                secondsTextView?.text = "%1$2d.%2$02d".format(sec, milli)
            } else if (milliSeconds < 360000) {
                secondsTextView?.text = "%1$2d:%2$02d.%3$02d".format(minute, sec, milli)
            } else if (milliSeconds >= 360000) {
                secondsTextView?.text =
                    "%1$2d:%2$02d:%3$02d.%4$02d".format(hour, minute, sec, milli)
            }
            //그리고 순위랑 국기는 나중에 더하자....
        }
    }

    //뷰홀더를 만듬. 껍데기임.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScoreRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.score_item, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        //50개만 반환하자.
        // rResult.size 이거는 너무 많다.
        return rResult.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val highScoreTable = rResult[position]
        //값이 있을 때만 바인드 한다. (없는데 바인드하면 오류가 뜨나보다...)
        if (highScoreTable != null) {
            holder.bind(highScoreTable)
        }
    }
}