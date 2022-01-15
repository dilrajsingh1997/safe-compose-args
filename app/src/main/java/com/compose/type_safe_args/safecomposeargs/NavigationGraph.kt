package com.compose.type_safe_args.safecomposeargs

import android.os.Parcel
import android.os.Parcelable
import androidx.navigation.NavHostController
import com.compose.type_safe_args.annotation.ArgumentProvider
import com.compose.type_safe_args.annotation.ComposeDestination
import com.compose.type_safe_args.annotation.HasDefaultValue

class NavigationGraph(private val navHostController: NavHostController) {
    val openUserPage: (Boolean, IntArray, ArrayList<String>, ArrayList<User>) -> Unit =
        { isLoggedIn, userIds, userNames, uniqueUsers ->
            navHostController.navigate(
                UserPage.getDestination(
                    isLoggedIn = isLoggedIn,
                    userIds = userIds,
                    userNames = userNames,
                    uniqueUsers = uniqueUsers
                )
            )
        }

    val openTncPage: () -> Unit = {
        navHostController.navigate(TncPage.getDestination())
    }

    val openEndScreen: (String) -> Unit = { endText ->
        navHostController.navigate(EndScreen.getDestination(endText))
    }
}

@ComposeDestination
interface UserPage {
    @HasDefaultValue
    val userId: String
    val isLoggedIn: Boolean
    val userIds: IntArray
    val userNames: ArrayList<String>

    @HasDefaultValue
    val uniqueUser: User
    val uniqueUsers: ArrayList<User>

    @ArgumentProvider
    companion object : IUserPageProvider {
        override val userId: String
            get() = "sample-user-id"
        override val uniqueUser: User
            get() = User(id = -1, name = "default")
    }
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
interface HomePage {
    companion object
}

@ComposeDestination
interface TncPage {
    @HasDefaultValue
    val tncUrl: String

    companion object
}

@ArgumentProvider
object TncPageProvider : ITncPageProvider {
    override val tncUrl: String
        get() = "www.sample-url.com"
}

@ComposeDestination
interface EndScreen {
    val endText: String

    companion object
}
