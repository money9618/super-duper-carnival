package com.millifruit.finddivisors

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.millifruit.finddivisors.myConstants.GMConstants
import com.millifruit.finddivisors.myConstants.IntentNameConstants
import com.millifruit.finddivisors.myConstants.TAG
import com.millifruit.finddivisors.myRealmDB.HighScoreTable
import com.millifruit.finddivisors.myRealmDB.LastFoundNumberTable
import com.millifruit.finddivisors.php.InsertData
import getDBTestTaskExecute
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer


class GameActivity : AppCompatActivity() {
    var tryCount = 0//나눌 수 있을 떄 인풋에 값이 있고 나눌 수 있을 때
    private var objectNumber = 17179869184L//2부터 시작. 다음을 누르면 1씩 증가.
    var objectDividedNumber = 0L//최초시작때 objectNumber로부터 값을 받음. 나누기를 할 떄마다 나눠짐. 소수판단은 이걸로 함.
    var inputNumber = 0L//오브젝트넘버를 나누는 숫자.
    var tryCountActivatedFlag = true//그냥 계속 트라이 표시하는 걸로...
    var existStringInInputNumberViewFlag = false//스트링이 인풋넘버에 있는지 없는지
    var numberIsDividedFlag = false//숫자가 나뉘어졌을 때, 숫자 이외의 버튼이 동작하지 않도록(실제로는, 나누기가 작동하지 않도록 함)
    var objectNumberDivisorSize = 0L//현재 숫자의 소수 약수 개수.(중복 포함)
    var numberOfdivisorArrayList = ArrayList<Long>()//텍스트뷰에 표시하기 위한 어레이리스트
    var sameNumberFlag = false//같은 수를 나누려고 할 때 세움.
    var timeFormatFlag = 0//0,1,2 이렇게 돌아가면서 타임포멧표시형식을 변경
    var lastDiVidedCount = 1//현재 몇번쨰 숫자인지 표시. 첫값은 1
    var endNumber = 0//나눠야하는 숫자 개수.

    //시계 표시를 위한 선언
    private var timerTask: Timer? = null//timer객체를 가리키는 참조변수
    private var milliSecondsValue = 0//0.01초마다 오르는 값.


    /**
     *
     *  게임을 구분하기 위한 플래그. 여러가지 상황에 쓰일 것이다.
     *gameModeFlag
     * GMConstants 내에서 숫자변경이 이루어져야한다.
     * 일찬 초기화로 기본모드로 설정한다. 나중에 받아 들였을 때 게임모드를 변경하면 된다.
     */
    private var gameModeFlag = GMConstants.NEW_GAME.toInt()

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //모바일광고SDK초기화.
        //lateinit var mAdView : AdView
        MobileAds.initialize(this) {}
        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        //이렇게 하면 광고가 나온다고 하네...

        //DB인스턴스 생성. 이제 필요에 따라 DB검색, 추가를 실행한다.
        //그냥 실행할때마다 열었다 닫았다 해보자...
        //realm = Realm.getDefaultInstance()
        //


        val gameIdentfyCode = intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)
        Log.d(
            TAG.GA,
            "왜 이게 뜰까${intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)}"
        )
        Log.d(
            TAG.GA, "글자는 이거임 : ${IntentNameConstants.GAME_IDENTIFY_CODE}"
        )

        //MainActivity에서 intent를 들고온다.(inline처리하는게 좋다고 안드로이드스튜디오차 추천해서 이렇게 표시했다...)
        //익숙해져야할듯.
        //게임이 클릭됬을 경우, 무슨 게임이 클릭됬는지 숫자를 받아와서 판단하여 게임을 실행한다.
        when (gameIdentfyCode) {

            GMConstants.NEW_GAME -> {
                //...시작넘버를 2로 설정한다.
                objectNumber = 2L
                //최초실행했더라도 숫자는 2부터 시작되기 때문에 저장해준다.
                saveNumberToLFNT(objectNumber, gameModeFlag)
            }

            //무한모드가 클릭됬다면...
            GMConstants.INFINITY_GAME -> {
                //...플래그를 1로 바꾸어서 무한모드를 활성화싴준다. 이게 서있는 이상, 다음 스테이지는 모두 랜덤숫자다.
                gameModeFlag = GMConstants.INFINITY_GAME.toInt()
                objectNumber = getRandomObjectNumber()

                //갯수를 보이지 않게 한다.
                last_number_count_view.visibility = View.INVISIBLE
            }

            //10모드가 클릭됬다면...
            GMConstants.TEN_NUMBER_GAME -> {
                //...플래그를 1로 바꾸어서 무한모드를 활성화싴준다. 이게 서있는 이상, 다음 스테이지는 모두 랜덤숫자다.
                gameModeFlag = GMConstants.TEN_NUMBER_GAME.toInt()
                objectNumber = getRandomObjectNumber()
                //10개까지 찾으면 게임 종료하게 함.
                endNumber = GMConstants.TEN_NUMBER_GAME_END_NUMBER//잠시 1로 바꿈....
            }

            //100모드가 클릭됬다면...
            GMConstants.HUNDRED_NUMBER_GAME -> {
                //...플래그를 1로 바꾸어서 무한모드를 활성화싴준다. 이게 서있는 이상, 다음 스테이지는 모두 랜덤숫자다.
                gameModeFlag = GMConstants.HUNDRED_NUMBER_GAME.toInt()
                objectNumber = getRandomObjectNumber()
                //10개까지 찾으면 게임 종료하게 함.
                endNumber = GMConstants.HUNDRED_NUMBER_GAME_END_NUMBER
            }

            //1000모드가 클릭됬다면...
            GMConstants.THOUSAND_NUMBER_GAME -> {
                //...플래그를 1로 바꾸어서 무한모드를 활성화싴준다. 이게 서있는 이상, 다음 스테이지는 모두 랜덤숫자다.
                gameModeFlag = GMConstants.THOUSAND_NUMBER_GAME.toInt()
                objectNumber = getRandomObjectNumber()
                //10개까지 찾으면 게임 종료하게 함.
                endNumber = GMConstants.THOUSAND_NUMBER_GAME_END_NUMBER
            }

            //게임을 이어서 한다면...
            GMConstants.CONTINUE -> {
                realm = Realm.getDefaultInstance()
                val realmResult = realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
                    .findFirst()//보여주고 싶은 DB결과를 선언
                //realmResult가 존재하는 객체일 경우...
                if (realmResult != null) {
                    //...데이터에서 불러와준다.
                    //...1L을 더하는 이유: 마지막으로 나눈 숫자 그다음의 숫자를 나눠야 하기 때문에 1을 더해줘야 한다.
                    objectNumber = realmResult.lastFoundNumber + 1L
                    Log.d(
                        TAG.GA,
                        "세이브 데이터 잘 불러왔니? objectNumber : ${objectNumber}, realmResult id,LSN,ED,UD : ${realmResult.id},${realmResult.lastFoundNumber},${realmResult.entryDate},${realmResult.updateDate}"
                    )
                    //...시간을 불러오기(초)
                } //존재하는 객체를 참조하는 경우...
                else {
                    //...존재하지 않을리가 없음. MainActivity에서 한번 체크하고 넘어온 것이기 때문에...
                }
                //...닫아줌그리고 인스턴스를 종료함.
                realm.close()

                //갯수를 보이지 않게 한다.
                last_number_count_view.visibility = View.INVISIBLE
            }

            else -> {
                Log.d(
                    TAG.GA,
                    "intent에 숫자가 안담겨 있음.... : ${intent.getStringExtra(IntentNameConstants.GAME_IDENTIFY_CODE)
                        ?.toInt()}"
                )
            }

        }


        try_count_text_view.text = "try : ${tryCount}"
        object_number_text_view.text = objectNumber.toString()
        input_number_text_view.text = inputNumber.toString()
        last_number_count_view.text = "${lastDiVidedCount}/${endNumber}"

        //초기설정
        gameStart(gameModeFlag)

        //게임이 시작됨가 동시에 타이머를 실행시킨다.
        startTimer()

        //타이뷰 클릭했을 때 동작설정
        time_View.setOnClickListener {
            //클릭하면 표시를 바꾼다.
            timeFormatFlag++
            //포멧이 3개니까 (0,1,2에할당했음) 3이상이 되버리면 다시 0으로 되돌린다. 순환.
            if (timeFormatFlag > 1) {
                timeFormatFlag = 0
            } //0,1일 경우...
            else {
                //...아무것도 하지 않는다.
            }

            //startTimer에서 자동으로 처리하게 만들어 놔서 필용 없는 코드임.
            Log.d("시간 포맷 변경", " timeFormatFlag : $timeFormatFlag")
        }
        
        button0.setOnClickListener {
            touchNumberButton("0", input_number_text_view)
        }
        button1.setOnClickListener {
            touchNumberButton("1", input_number_text_view)
        }
        button2.setOnClickListener {
            touchNumberButton("2", input_number_text_view)
        }
        button3.setOnClickListener {
            touchNumberButton("3", input_number_text_view)
        }
        button4.setOnClickListener {
            touchNumberButton("4", input_number_text_view)
        }
        button5.setOnClickListener {
            touchNumberButton("5", input_number_text_view)
        }
        button6.setOnClickListener {
            touchNumberButton("6", input_number_text_view)
        }
        button7.setOnClickListener {
            touchNumberButton("7", input_number_text_view)
        }
        button8.setOnClickListener {
            touchNumberButton("8", input_number_text_view)
        }
        button9.setOnClickListener {
            touchNumberButton("9", input_number_text_view)
        }


        clear_button.setOnClickListener {

            //진행중이면서 숫자로 나눠지지 않았다면...
            if (!numberIsDividedFlag) {
                input_number_text_view.text = "0"
            }
            //나눠졌다면...
            else {
                //...아무거소 하지 않는다.
            }


        }


        divide_button.setOnClickListener {
            //같은 숫자를 나눠버렸을 때...동작하지 않도록 함.
            if (!sameNumberFlag) {
                //문자열이 들어가지 않았을 때.... 만 동작하도록 함.
                if (!existStringInInputNumberViewFlag) {
                    //...동작 가부만 판단함. 따로 처리하는 것 없음.
                    var inputNumberToDouble = input_number_text_view.text.toString().toDouble()
                    //inputNumber가 Long형 허용범위를 내에 있을 때...
                    if (!isBigThanLong(inputNumberToDouble)) {
                        //...허용범위 내에 있는 숫자를 입력받았기에 숫자 대입 가능.
                        var inputNumber = input_number_text_view.text.toString().toLong()
                        //inputNumber가 0이 아니라면...
                        if (inputNumber != 0L) {
                            ///남은 숫자랑 나누는 숫자가 같지 않다면...
                            if (inputNumber != objectDividedNumber) {
                                //input이 1이 아니라면
                                if (inputNumber != 1L) {
                                    //나눠진다면 ...
                                    if (objectDividedNumber % inputNumber == 0L) {
                                        //...오브젝트 디바이드 넘버를 인풋넘버로 나눈다.
                                        objectDividedNumber /= inputNumber
                                        //...나뉘어졌기 때문에 숫자버튼만 눌러질 수 있도록 클리어를 비활성화 한다.
                                        numberIsDividedFlag = true


                                        //...나뉘어졌기 때문에 detailView를 수정한다.
                                        numberOfdivisorArrayList[numberOfdivisorArrayList.size - 1] =
                                            inputNumber
                                        numberOfdivisorArrayList.add(objectDividedNumber)
                                        number_details_text_view.text = detailNumberStringInDetail(
                                            numberOfdivisorArrayList
                                        )

                                        //최종숫자가 소수라면...
                                        if (isPrimeNumber(objectDividedNumber)) {

                                            existStringInInputNumberViewFlag = true

                                            saveNumberToLFNT(objectNumber,gameModeFlag)
                                            //게임을 종료,기록하는 팝업으로 빠진다.
                                            nextOrSave()
                                            //게임종류에 따른 게임계속또는 종료 판단.
                                            //gameStart(gameModeFlag)
                                            //소수인지 판단한다.


                                        }
                                        //최종숫자가 더 나뉠 수 있다면...
                                        else {
                                            //...detail에 추가한다.
                                            //"나뉘어졌습니다. 새로운 수로 나눠주세요."//...input을 0으로 설정
                                            status_text_view.text = "${objectNumber}=n×${inputNumber}"
                                            existStringInInputNumberViewFlag = true
                                        }

                                    }
                                    //나눠지지 않는다면...
                                    else {
                                        //...나눌수 없다고 메세지 표시. 그리고 텍스트안 숫자 0으로 초기화.
                                        status_text_view.text = " ≠n×${inputNumber}"
                                        input_number_text_view.text = "0"
                                    }
                                    //tryCount가 살아있다면...
                                    if (tryCountActivatedFlag) {
                                        tryCount++
                                        try_count_text_view.text = "try : ${tryCount}"
                                    }
                                    //1로 나눌려고 한다면...
                                } else {
                                    status_text_view.text = "...no ÷1"
                                    input_number_text_view.text = "0"//...input을 0으로 설정
                                }

                                //동일 숫자를 나누려고 한다면..
                            } else {
                                //...동일 숫자는 나눌 수 없다고 표시.
                                status_text_view.text =
                                    "...no ${inputNumber}÷${inputNumber}"
                                sameNumberFlag = true


                            }
                            //inputNumber가 0이라면...
                        } else {
                            //...status_text_view에 나눌 수 없다고 표시.
                            status_text_view.text = "...no ÷0"
                        }
                        //inputNumber가 Long허용범위를 넘어버버린 수를 입력받은 채 나누기 버튼을 눌렀을 때
                    } else {
                        status_text_view.text = "...to big to divide"
                    }

                    //문자열이 들어가쓸 때에는...
                } else {
                    //...동작하지 않는다.
                }
                //같은숫자를 누르고 난 후에는...
            } else {
                //아무처리도 하지 않는다.
                //숫자버튼을 눌러서 해제시켜야 함.(숫자버튼을 누르면 false로 다시 되돌아감.)
            }
        }
    }


    /**
     *숫자버튼을 눌렀을 때에 textview에 반영하는 것.
     * @param buttonNumber 누른 버튼이 해당하는 숫자.
     * @param inputNumberTextView 누른 뷰(버튼)
     */
    private fun touchNumberButton(buttonNumber: String, inputNumberTextView: TextView) {
        //숫자버튼을 눌러줬다면 지우기가 활성화되도록 플래그를 false로 돌려준다.
        numberIsDividedFlag = false

            //inputNumberTextView에 문장이 들어갔을 때...
            if (existStringInInputNumberViewFlag) {
                //...텍스트뷰를 0으로 강제 설정해준다.
                inputNumberTextView.text = "0"
                //...플래그를 원래대로 돌려준다.(꺼준다.)
                existStringInInputNumberViewFlag = false
            }

            //input의 숫자가 0이 아닐때...
            if (inputNumberTextView.text.toString() != "0") {
                //숫자가 있으면서 같은 숫자를 나누려고 시도한 후에 버튼을 눌렀을 때...
                if (sameNumberFlag) {
                    //...누른 버튼숫자를 뷰에 그대로 반영해준다.(초기화되는 효과를 볼 수 있음.)
                    inputNumberTextView.text = buttonNumber
                    //...플래그 false로 해제.(나누기 버튼이 동작하도록 플래그를 되돌려놓는다.)
                    sameNumberFlag = false
                }//같은 숫자로 나눴다는 처리를 하지 않은 상태에서 버튼을 눌렀을 때
                else {
                    //...숫자에 오른쪽에 이어서 더해준다.
                    inputNumberTextView.text = inputNumberTextView.text.toString() + buttonNumber
                }
            }
            //숫자가 0일 때...(=0이면서 한 자리 수일 때)
            else {
                //...입력방느 숫자를 표시해준다.
                inputNumberTextView.text = buttonNumber
            }
    }


    /**
     * 소수인지 판별한다.
     * 소수면 true. 아니면 false
     *@param objectNumber
     *
     * @return true if objectNumber is primeNumber
     *
     */
    private fun isPrimeNumber(objectNumber: Long): Boolean {
        var result: Boolean = true
        for (i in 2L until objectNumber - 1L) {
            if ((objectNumber % i) == 0L) {
                result = false
                break
            }
        }
        return result
    }

    /**
     * 약수의 개수를 Long형으로 반환(소수 중복 계산)
     * @param objectNumber
     * return objectNumber의 약수의 개수를 하나하나 카운트해서 내보낸다.
     */
    private fun divisorCount(objectNumber: Long): Long {
        //result로 반환
        var result: Long = 0L
        //굴릴 숫자를 temp로 옮겨준다.temp로 굴린다.
        var temp = objectNumber
        //나눌 숫자.(2,,.....2,3,...2,3,5.....)
        var i = 2L
        do {
            //temp가 i로 나눠지면...
            if (temp % i == 0L) {
                Log.d("divisorCount", "temp%i==0L...${temp}%${i}==0L")
                //...result++
                result++
                //...temp를 i로 나눈 결과로 만들어 준다.
                temp /= i
                //...다시 초기화 시켜준다.
                i == 2L
            } else {
                i++
            }
            //...temp가 1이 됬을 때 종료(1이 아니면 true...즉 계속돌림.더이상 나눌 수 없을 때)
        } while (temp != 1L)
        return result
    }


    /**
     * detailsView에 수식을 표현하기 위한 스트링 반환
     * @param objectNumber 기본이 되는 숫자.
     * @param numberOfdivisorArrayList 표현하고 싶은 숫자가 들어있는 리스트
     *
     * @return 스트링을 출력해준다.
     * ex)'220= 2 x 2 x 55' 꼴로 출력됨. 맨 오른쪽은 제일 큰숫자(=나누고 남은 숫자)가 됨.
     */
    private fun detailNumberStringInDetail(
        numberOfDivisorArrayList: ArrayList<Long>
    ): String {
        var result = ""
        //어레이의 사이즈(=a.size)만큼 for문을 실행한다.
        for (i in 0 until numberOfDivisorArrayList.size) {
            //최초 i는 숫자 그대로 String에 붙여줌.
            result = if (i == 0) {
                numberOfDivisorArrayList[i].toString()//...하나만 내놓는다.....ex)  110..... 최초숫자는 숫자만 뜨게 함.
            } else if(i == numberOfDivisorArrayList.size-1){
                "${result}x \n ${numberOfDivisorArrayList[i]}"
            }
            //최초가 아닐 경우...
            else {
                "${result}x${numberOfDivisorArrayList[i]}"// '2 x 5'.... '2 x 5 x 110'...꼴로 표현되게 됨.
            }
        }
        return result// '220= 2 x 2 x 55' 꼴로 출력됨. 맨 오른쪽은 제일 큰숫자(=나누고 남은 숫자)가 됨.
    }

    private fun detailNumberStringInStatus(
        numberOfDivisorArrayList: ArrayList<Long>
    ): String {
        var result = ""
        //어레이의 사이즈(=a.size)만큼 for문을 실행한다.
        for (i in 0 until numberOfDivisorArrayList.size) {
            //최초 i는 숫자 그대로 String에 붙여줌.
            result = if (i == 0) {
                numberOfDivisorArrayList[i].toString()//...하나만 내놓는다.....ex)  110..... 최초숫자는 숫자만 뜨게 함.
            }
            //최초가 아닐 경우...
            else {
                "${result}x${numberOfDivisorArrayList[i]}"// '2 x 5'.... '2 x 5 x 110'...꼴로 표현되게 됨.
            }
        }
        return result// '220= 2 x 2 x 55' 꼴로 출력됨. 맨 오른쪽은 제일 큰숫자(=나누고 남은 숫자)가 됨.
    }



    /**
     * Long형 숫자보다 클경우 true
     * @param inputNumber 입력받은 숫자.
     *
     * @return Long최고형과 비교해서 inputNumber가 크면 true반환.
     *
     * 참고로 Long 범위는 아래와 같음.
     * -9223372036854775808 ~ 9223372036854775807
     */
    private fun isBigThanLong(inputNumber: Double): Boolean {
        var result = false
        val doubleNumber: Double = 9223372036854775807.0
        if (inputNumber > doubleNumber) {
            result = true
        }
        return result
    }


    /**
     * Long난수를 취득. 게임난이도도 여기서 설정.
     * 2~19223372036854775807
     * 19,223,372,036,854,775,807
     */
    private fun getRandomObjectNumber(): Long {
        //입력받은 숫자크기

        //...무한으로 하고 싶을 떄 이걸로 함.
        return when (gameModeFlag) {
            GMConstants.INFINITY_GAME.toInt() -> randomNumberWPN(2, 100000000)
            GMConstants.TEN_NUMBER_GAME.toInt() -> randomNumberWPN(10, 100)
            GMConstants.HUNDRED_NUMBER_GAME.toInt() -> randomNumberWPN(100, 1000)
            GMConstants.THOUSAND_NUMBER_GAME.toInt() -> randomNumberWPN(1000, 10000)
            else -> 99999
        }
        // return randomNumber(2, 10000)

    }

    /**
     * 소수를 포함한 랜덤숫자 반환
     * @param start 시작숫자.
     * @param end 끝숫자.
     *
     * @return Long형의 랜덤숫자.
     */
    private fun randomNumber(start: Long, end: Long): Long {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    /**
     * 소수를 제외한 랜덤숫자 반환
     * @param start 시작숫자.
     * @param end 끝숫자.
     *
     * WPM : Without Prime Number
     *@return Long형의 랜덤숫자.
     */
    private fun randomNumberWPN(start: Long, end: Long): Long {
        require(start <= end) { "Illegal Argument" }
        var result: Long
        do {
            result = randomNumber(start, end)
        } while (isPrimeNumber(result))
        return result
    }


    /**
     * 타이머를 시작함.
     */
    private fun startTimer() {
        // timer에서는 전달받은 함수를 0.01초에 한 번씩 호출
        timerTask = timer(period = 10) {
            milliSecondsValue++
            val hour = milliSecondsValue / 360000 //한시간은 3600.00초니까...
            val minute =
                (milliSecondsValue % 360000) / 6000// 한시간단위는 다 나눠어 때버리고 나머지를 60으로 나눠서 값을 낸다.
            val sec = (milliSecondsValue / 100) % 60 // 1초가 지나면 time은 100이되는데 이 때 sec은 1이 됨
            val milli = milliSecondsValue % 100 // milli는 1~99까지만 출력되어야 하니 100으로 나눈 나머지임

            // runonuithread메소드 호출 (ui관련 객체를 전달) <sam변환>
            // 힘든 작업을 할 때는 워커 스레드에서 작용하는데 timer함수가 그렇다.
            // 워커 스레드 작업 중에는 ui변경이 불가능하다. 하지만 runonuithread에 전달되는 클래스는 워커 스레드에서 벗어난다.
            runOnUiThread {
                when (timeFormatFlag) {
                    //초만 표현할 떄...
                    0 -> {
                        //모든 것을 초로 표현해야하니까...이렇게 붙임
                        time_View.text = "%1d".format((milliSecondsValue / 100))
                    }
                    //00:00:00표시를 하게 될 때....
                    1 -> {
                        time_View.text =
                            "%1$02d:%2$02d:%3$02d.%4$02d".format(hour, minute, sec, milli)
                    }
                    //아무것도 표현하지 않을 때...
                    2 -> {
                        //...아무것도 하지 않음.
                    }
                    //아무것도 표현하지 않을 때...
                    else -> {
                        //...아무것도 하지 않음.
                    }
                }
            }
        }
    }

    /**
     * 타이머를 정지함.
     */
    private fun pauseTimer() { // 타이머 일시정지
        // Timer객체의 cancel메소드로 타이머 중지 가능
        timerTask?.cancel() // 실행중인 타이머가 있다면 취소한다.
    }


    /**
     * DB에 초를 저장.
     */
    //  private fun saveSecondsToDB() {
    //게임모드가 컨티뉴일 때만 발동하게 한다.
    //      Log.d("saveSeconds", "gameModeFlag: $gameModeFlag")
    //      Log.d("saveSeconds", "게임모드 번호: ${GMConstants.CONTINUE}")
    //      Log.d("saveSeconds", "objectNumber: ${GMConstants.CONTINUE}")

    //      if (gameModeFlag == GMConstants.CONTINUE.toInt()) {
    //          //인스턴스 생성
    //          realm = Realm.getDefaultInstance()
    //          val realmResult = realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
    //              .findFirst()//보여주고 싶은 DB결과를 선언
    //          //
    //          realm.executeTransaction {
    //              if (realmResult != null) {
    //                 realmResult.seconds = timeValue
    //             }
    //         }
    //     }//게임모드가 컨티뉴 이외일 경우 실행하는 게 없다.
    //     else {
    //         //저장하지 않는다.
    //      }
    //      realm.close()
    //   }

    /**
     * 방금 찾아낸 수를
     *
     * @param objectNumber 저장할 숫자
     * @param gameModeFlag 게임모드 판단
     * LastFoundNumberTable에 저장함.
     *LFNT: LastFoundNumberTable
     */
    private fun saveNumberToLFNT(objectNumber: Long, gameModeFlag: Int) {
        //infinityGame이 아닐 경우... 즉 플래그가 1이 아닐경우.... 저장한다.
        if (gameModeFlag == GMConstants.CONTINUE.toInt() || gameModeFlag == GMConstants.NEW_GAME.toInt()) {
            realm = Realm.getDefaultInstance()
            val realmResult = realm.where(LastFoundNumberTable::class.java).equalTo("id", 1L)
                .findFirst()//보여주고 싶은 DB결과를 선언
            //realmResult가 존재하는 객체일 경우...
            if (realmResult != null) {

                //...트랜잭션을 실행.
                //......실행하고 나서야 값의 조작이 가능하다.
                realm.executeTransaction {
                    realmResult.lastFoundNumber = objectNumber
                    realmResult.updateDate = Date()
                }
                Log.d(
                    "objectNumber Saved ",
                    " objectNumber : ${objectNumber}, realmResult id,LSN,ED,UD : ${realmResult.id},${realmResult.lastFoundNumber},${realmResult.entryDate},${realmResult.updateDate}"
                )
            } //존재하는 객체를 참조하는 경우...
            else {
                //...존재하지 않을리가 없음. MainActivity에서 한번 체크하고 넘어온 것이기 때문에...
            }
            //...닫아줌그리고 인스턴스를 종료함.
            realm.close()

        } //무한모드일 경우...
        else {
            //...저장하지 않는다...즉 아무처리도 하지 않는다.
        }
    }


    private fun nextOrSave() {
        //숫자를 모두 찾은 뒤, 게임 시작 전의 처리할 프로세스를 여기에 적는다.
        lastDiVidedCount++//플러스해준다. 이숫자는 GameStart가 실행됬을 때 뷰에 반영된다..

        //최종나눈 숫자를 아래에 넘겨준다.
        if(isPrimeNumber(objectNumber)){
            status_text_view.text= " ${objectNumber}= PN"
            }else{
            status_text_view.text= objectNumber.toString() + "=" + detailNumberStringInStatus(numberOfdivisorArrayList)
        }

        //게임모드가 기록모드인경우AND숫자가 오버했을 경우...
        if ((gameModeFlag == GMConstants.TEN_NUMBER_GAME.toInt() ||
                    gameModeFlag == GMConstants.HUNDRED_NUMBER_GAME.toInt() ||
                    gameModeFlag == GMConstants.THOUSAND_NUMBER_GAME.toInt()) && lastDiVidedCount > endNumber
        ) {
            //로그출력
            Log.d("숫자 10개 게임종료", "\"${lastDiVidedCount}/${endNumber}\"")

            //...더이상 나눌 수 없다고 텍스트뷰에 표기.
            input_number_text_view.text =
                "0"//...input을 0으로 설정

            //타이머 정지
            pauseTimer()

            //세이브창을 띄운다.
            saveScoreToDB()
            //finish()
            //
        }
        //아직 숫자가 남았을 경우...
        else {
            //...게임을 계속한다.
            gameStart(gameModeFlag)
        }
    }

    /**
     * 다이알로그를 띄워서 DB에 저장한다.
     */
    private fun saveScoreToDB() {
        //게임기록 저장
        //기록갱신을 위해서 이름과 설명을 입력받고, 렝킹DB에 추가함.
        //추가된 레코드는 바로 전송됨
        //스코어보드로 이동.(게임창은 닫힘)
        //스코어보드에서는
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.game_finished_dialog, null)

        val nameEdit = dialogView.findViewById<EditText>(R.id.name_dialog_edit)
        val messageEdit = dialogView.findViewById<EditText>(R.id.comment_dialog_edit)

        //다이알로그를 만들고 실행시켜서 정보를 입력하게 한 후, 오케이를 누르면 DB레코드를 생성한다.
        builder.setView(dialogView)
            .setPositiveButton("확인") { _, _ ->

                //var id = ...아이디는 1씩 증가시킬꺼기 때문에 ㄱㅊ....
                var name: String? = null
                var message: String? = null
                val gameMode = gameModeFlag
                val seconds = milliSecondsValue
                val country = getCountryCodeValue()
                val entryDate: Date = Date()

                //공백이나 눌이 아닐떄...값을 더해준다.솔직히 의미가 있는지 잘 몰겠슴.
                if (!nameEdit.text.isNullOrEmpty()) {
                    name = nameEdit.text.toString()
                }
                if (!messageEdit.text.isNullOrEmpty()) {
                    message = messageEdit.text.toString()
                }



                //realm 저장.
                //내부.
                realm = Realm.getDefaultInstance()
                realm.executeTransaction {
                    val maxId = realm.where<HighScoreTable>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L

                    val highScoreTable = realm.createObject<HighScoreTable>(nextId)
                    highScoreTable.gameMode = gameMode
                    highScoreTable.seconds = seconds
                    if (country != null) {
                        highScoreTable.country = country
                    }
                    if (name != null) {
                        highScoreTable.name = name
                    }
                    if (message != null) {
                        highScoreTable.message = message
                    }
                    highScoreTable.entryDate = entryDate
                }

                Toast.makeText(
                    applicationContext,
                    "DB에 저장했습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                //mySQL저장.
                //외부
                //$name, $message, $seconds, $country, $gameMode);
                val insertTask = InsertData()
               insertTask.execute(
                   "http://finddivisors.dothome.co.kr/highScoreRegister.php",
                   name,
                   message,
                   seconds.toString(),
                   country,
                   gameMode.toString()
                )

                val insertTask2 = InsertData()
                insertTask2.execute(
                    "http://finddivisors.dothome.co.kr/highScoreTestRegister.php",
                    name,
                    message,
                    seconds.toString(),
                    country,
                    gameMode.toString()
                )


                realm.executeTransaction {
                    realm.where(HighScoreTable::class.java).findAll().deleteAllFromRealm()
                    Log.d("getHSDB", "내부DB삭제")
                }

                //getDBTestTaskExecute("http://finddivisors.dothome.co.kr/getHighScoresJson.php")
                getDBTestTaskExecute("http://finddivisors.dothome.co.kr/getHighScoresTestJson.php")

                /* 확인일 때 main의 View의 값에 dialog View에 있는 값을 적용 */
                //게임을 종료하고, 기록창을 띄움.
                finish()
            }
            .setNegativeButton("취소") { _, _ ->
                /* 취소일 때 아무 액션이 없으므로 빈칸 */


                //게임을 종료함.
                //기록창을 띄워야하나?
                finish()
            }
            .show()
    }

    //국가를 따오는 것.
    private fun getCountryCodeValue(): String {
        val tm =
            this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkCountryIso
    }


    //게임이 시작됬을떄(또는 이어서 계속 시작될 때)
    private fun gameStart(gameModeFlag: Int) {
//플래그에 따라서 다음 숫자를 지정해준다.
        when (gameModeFlag) {
            GMConstants.INFINITY_GAME.toInt() -> objectNumber = getRandomObjectNumber()
            GMConstants.TEN_NUMBER_GAME.toInt() -> objectNumber = getRandomObjectNumber()
            GMConstants.HUNDRED_NUMBER_GAME.toInt() -> objectNumber =
                getRandomObjectNumber()
            GMConstants.THOUSAND_NUMBER_GAME.toInt() -> objectNumber =
                getRandomObjectNumber()
            //무한모드가 아닐 경우(일반 1++의 게임일 경우.)
            else -> objectNumber++
        }


        //무한모드, 비무한모드의 공통처리.
        object_number_text_view.text = objectNumber.toString()


        //게임을 시작하기 전에 종료를 할지 말지를 판단한다.
        last_number_count_view.text = "${lastDiVidedCount}/${endNumber}"
        objectDividedNumber = objectNumber//오브젝트 넘버를 실제 숫자가 나눠질 디바이디드넘버에 넣어준다.
        objectNumberDivisorSize = divisorCount(objectNumber)//디바이디드 넘버의 사이즈를 알아본다.
        numberOfdivisorArrayList.clear()//'100= 2*2*5*5' 등에 표시에 필요한 표시할 어레이를 클리어 해준다.
        numberOfdivisorArrayList.add(0, objectNumber)//첫번째 아이템에 objectNumber를 넣어준다. 기준점을 위해서....
        Log.d("GameStart", "objectNumber: $objectNumber")
        Log.d("GameStart", "현재숫자의 약수 개수...${objectNumberDivisorSize}")
        input_number_text_view.text = "0"//인풋뷰를 초기화 해준다.
        number_details_text_view.text =
            detailNumberStringInStatus(numberOfdivisorArrayList) //detailview를 초기화해준다.
        //뜬 숫자가 소수일 경우... 넘긴다.
        if(isPrimeNumber(objectNumber)){
            nextOrSave()
        }
    }


    override fun onStop() {
        super.onStop()
        //타이머를 정지
        timerTask?.cancel()

    }

    override fun onStart() {
        super.onStart()
        //타이머를 재실행
    }

}