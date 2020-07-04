package com.millifruit.finddivisors.php

import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.android.volley.VolleyLog.TAG
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


internal class InsertData :
    AsyncTask<String?, Void?, String>() {
    var progressDialog: ProgressDialog? = null
    override fun onPreExecute() {
        super.onPreExecute()
       // progressDialog = ProgressDialog.show(
       //     this@MainActivity,
       //     "Please Wait", null, true, true
       // )
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        progressDialog?.dismiss()
       // mTextViewResult.setText(result)
        Log.d(TAG, "POST response  - $result")
    }

    /**
    $name, $message, $seconds, $country, $gameMode);
     */
    override fun doInBackground(vararg params: String?): String? {
        //아래 pstParameters를 수정.
        //변수에 맞게 집어넣어준다.
        //1.순서대로.
        //2.변수명 그대로.
        val postParameters = "name=${params[1]}&message=${params[2]}&seconds=${params[3]}&country=${params[4]}&gameMode=${params[5]}"
        Log.d(TAG,"postParameters : $postParameters")
        return try {
            val url = URL(params[0])
            val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection// URL화 한다.
            httpURLConnection.readTimeout = 5000
            httpURLConnection.connectTimeout = 5000
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.connect()
            val outputStream: OutputStream = httpURLConnection.outputStream
            outputStream.write(postParameters.toByteArray(charset("UTF-8")))
            outputStream.flush()
            outputStream.close()
            val responseStatusCode: Int = httpURLConnection.responseCode
            Log.d(TAG, "POST response code - $responseStatusCode")
            val inputStream: InputStream
            inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                httpURLConnection.inputStream  //input스트림 개방
            } else {
                httpURLConnection.errorStream  //input스트림 에러?이건 모르겠다...
            }
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)
            val sb = StringBuilder()
            var line: String? = null
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            bufferedReader.close()
            sb.toString()
        } catch (e: Exception) {
            Log.d(TAG, "com.sjpark9618.finddivisor.php.InsertData: Error ",e).toString()
        }
    }
}