package com.millifruit.finddivisors.myRealmDB

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class HighScoreTable : RealmObject() {
    @PrimaryKey
    var id: Long = 0//아이디. 시간을 집어넣을 거임.
    var gameMode :Int = 0//게임모드를 받아와서 저장함.
    var seconds : Int =0;//걸린 시간... 이것으로 경쟁을 하게 됨.
    var country : String? = null//나라에 대한 것을 상수화 시켜서 그것으로 불러와서 아이템뷰에 표시할 거임.
    var name : String = ""//기록자 이름
    var message : String = ""//기록자의 메모. 한마디.
    //정보의 들어옴과 수정을 알기 위한 덧붙임 column 2개
    var entryDate: Date = Date() //등록일
    var updateDate: Date = Date() //수정일
}