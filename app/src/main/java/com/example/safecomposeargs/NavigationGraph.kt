package com.example.safecomposeargs

import android.os.Parcel
import android.os.Parcelable
import androidx.navigation.NavHostController
import com.example.annotation.ComposeDestination

class NavigationGraph(private val navHostController: NavHostController) {
    val openUserPage: (String, Boolean) -> Unit = { userId, isLoggedIn ->
        navHostController.navigate(UserPageDestination.getDestination(userId, isLoggedIn))
    }

    val openTncPage: (String) -> Unit = { tncUrl ->
        navHostController.navigate(TncDestination.getDestination(tncUrl))
    }

    val openEndScreen: (String) -> Unit = { endText ->
        navHostController.navigate(EndScreenDestination.getDestination(endText))
    }
}

@ComposeDestination(route = "userPage")
abstract class UserPage {
    abstract val userId: String
    abstract val isLoggedIn: Boolean
}

@ComposeDestination(route = "homePage")
abstract class HomePage {
}

@ComposeDestination(route = "tnc")
abstract class TncPage {
    abstract val tncUrl: String
}

@ComposeDestination(route = "endScreen")
abstract class EndScreen {
    abstract val endText: String
}

//@ComposeDestination(route = "test")
//abstract class test {
//    abstract val temp: temp
//}

data class temp(val xx: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(xx)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<temp> {
        override fun createFromParcel(parcel: Parcel): temp {
            return temp(parcel)
        }

        override fun newArray(size: Int): Array<temp?> {
            return arrayOfNulls(size)
        }
    }

}
