import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.millifruit.finddivisors.ScoreActivity
import com.millifruit.finddivisors.myRealmDB.HighScoreTable
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class GetHighScoresDBData :
    AsyncTask<String?, Void?, String?>() {
    var progressDialog: ProgressDialog? = null
    private var errorString: String? = null
    private var mJsonString: String = "123"
    private val TAG: String = "getHSDB"
    private lateinit var realm: Realm
    //var recyclerViewAdapter : ScoreRecyclerViewAdapter
    var doneFlag =false


   // class GetHighScoresDBData(recyclerViewAdapter: ScoreRecyclerViewAdapter){
   //
   // }


    override fun onPreExecute() {
        //super.onPreExecute()
       // progressDialog = ProgressDialog.show(
       //     MainActivity., "please wait...", "soon...", true)

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // progressDialog!!.dismiss()
        //mTextViewResult.setText(result)
        Log.d(TAG, "result : $result")
        if (result == null) {
            //     mTextViewResult.setText(errorString)
        } else {
            mJsonString = result
            showSearchResult()



            //엑티비티 실행.
            startActi()


    doneFlag=true
        }
    }

    override fun doInBackground(vararg params: String?): String? {
        val serverURL = params[0]
        // val postParameters = params[1]
        return try {
            val url = URL(serverURL)
            val httpURLConnection =
                url.openConnection() as HttpURLConnection
            httpURLConnection.readTimeout = 5000
            httpURLConnection.connectTimeout = 5000
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.doInput = true
            httpURLConnection.connect()
            val outputStream: OutputStream = httpURLConnection.outputStream
            //   if (postParameters != null) {
            //      outputStream.write(postParameters.toByteArray(charset("UTF-8")))
            //   }
            outputStream.flush()
            outputStream.close()
            val responseStatusCode = httpURLConnection.responseCode
            Log.d(TAG, "response code - $responseStatusCode")
            val inputStream: InputStream
            inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                httpURLConnection.inputStream
            } else {
                httpURLConnection.errorStream
            }
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)
            val sb = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            bufferedReader.close()
            sb.toString().trim { it <= ' ' }
        } catch (e: Exception) {
            Log.d(TAG, "GetData : Error ", e)
            errorString = e.toString()
            null
        }
    }

    private fun showSearchResult() {
        val TAG_JSON = "ranking"
        val TAG_NAME = "name"
        val TAG_MESSAGE = "message"
        val TAG_SECONDS = "seconds"
        val TAG_COUNTRY = "country"
        val TAG_ENTRY_DATE = "entryDate"
        val TAG_GAME_MODE = "gameMode"

        try {
            val jsonObject = JSONObject(mJsonString)
            val jsonArray: JSONArray = jsonObject.getJSONArray(TAG_JSON)
            for (i in 0 until jsonArray.length()) {
                //이하,Json에서 값을 가져와서 변수에 저장한다. 하나하나 이사하는 셈.
                val item = jsonArray.getJSONObject(i)
                val name = item.getString(TAG_NAME)
                val message = item.getString(TAG_MESSAGE)
                val seconds = item.getInt(TAG_SECONDS)
                val country = item.getString(TAG_COUNTRY)
                val entryDate = item.getString(TAG_ENTRY_DATE)
                val gameMode = item.getInt(TAG_GAME_MODE)

                //위에서 뽑아온 정보를, 오브젝트를 만들어서 거기에 넣어준다.
                realm = Realm.getDefaultInstance()
                realm.executeTransaction {
                    val maxId = realm.where<HighScoreTable>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    val highScoreTable = realm.createObject<HighScoreTable>(nextId)

                    highScoreTable.name = name

                    highScoreTable.message = message

                    highScoreTable.seconds = seconds
                    highScoreTable.country = country
                    highScoreTable.entryDate =
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(
                            entryDate.trim()
                        )!!
                    highScoreTable.gameMode = gameMode
                    //로그출력. DB에 잘 추가됬는지......
                    Log.d(
                        TAG,
                        "Json에서 값을 불러와서 realm에 추가합니다 : ${name},${message},${seconds},${country},${entryDate},${gameMode}"
                    )

                }


            }
        } catch (e: JSONException) {
            Log.d(TAG, "값을 못불러왔니...showResult : $mJsonString", e)
        }
    }

    private fun startActi(){

    }
}

fun getDBTestTaskExecute(url: String){
    val getDBTestTask = GetHighScoresDBData()//겟하이스코어데이터 선언.
    getDBTestTask.execute(url)//php넣어서 실행하게 한다.
}
