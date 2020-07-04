package com.millifruit.finddivisors

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

//Application을 승계하는 커스텀 클래스를 작성.

//Application클래스의 onCreate메소드는 MainActivity보다 먼저 호출됨.
//그러나 만든 것 만으로는 실행되지 않음.
//본 CustomApplication클래스를 어플 기동 시 실행되게 하기 위해서는, AndroidManifest.xml에 application탭에 추가기재 해야한다.


class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate() //Application의 onCreate를 실행.
        Realm.init(this)
        val config = RealmConfiguration
            .Builder()
            .name(Realm.DEFAULT_REALM_NAME)
            .deleteRealmIfMigrationNeeded()
            .schemaVersion(1)
            .build()
        Realm.setDefaultConfiguration(config)
    }
}