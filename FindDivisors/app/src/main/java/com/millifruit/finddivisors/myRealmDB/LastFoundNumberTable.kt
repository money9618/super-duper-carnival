package com.millifruit.finddivisors.myRealmDB


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

//이건 모델클래스
//open ...승계를 받기위해  class앞에 붙임
open class LastFoundNumberTable : RealmObject() {
    @PrimaryKey
    var id: Long = 0//아이디
    var lastFoundNumber : Long = 0L //책가격. 정수표현을 위해 Long=0으로 설정
    var seconds : Int =0;//걸린 시간...

    //정보의 들어옴과 수정을 알기 위한 덧붙임 column 2개
    var entryDate: Date = Date() //등록일
    var updateDate: Date = Date() //수정일

    //Byte, hort, Int, Long ...Realm안에서는 모두 Long으로 취급됨.

}

