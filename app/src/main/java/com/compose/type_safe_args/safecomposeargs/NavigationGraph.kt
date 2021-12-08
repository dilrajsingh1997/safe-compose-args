package com.compose.type_safe_args.safecomposeargs

import android.os.Parcel
import android.os.Parcelable
import androidx.navigation.NavHostController
import com.compose.type_safe_args.annotation.ComposeDestination

class NavigationGraph(private val navHostController: NavHostController) {
    val openUserPage: (String, Boolean, IntArray, ArrayList<String>, User, ArrayList<User>) -> Unit = { userId, isLoggedIn, userIds, userNames, uniqueUser, uniqueUsers ->
        navHostController.navigate(UserPageDestination.getDestination(userId, isLoggedIn, userIds, userNames, uniqueUser, uniqueUsers))
    }

    val openTncPage: (String) -> Unit = { tncUrl ->
        navHostController.navigate(TncPageDestination.getDestination(tncUrl))
    }

    val openEndScreen: (String) -> Unit = { endText ->
        navHostController.navigate(EndScreenDestination.getDestination(endText))
    }
}

@ComposeDestination
abstract class UserPage {
    abstract val userId: String
    abstract val isLoggedIn: Boolean
    abstract val userIds: IntArray
    abstract val userNames: ArrayList<String>
    abstract val uniqueUser: User
    abstract val uniqueUsers: ArrayList<User>
}

data class User(
    val id: Int,
    val name: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

@ComposeDestination
abstract class HomePage {
}

@ComposeDestination
abstract class TncPage {
    abstract val tncUrl: String
}

@ComposeDestination
abstract class EndScreen {
    abstract val endText: String
}
