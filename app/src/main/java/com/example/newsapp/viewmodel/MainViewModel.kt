import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel(private val isUserSubscribedUseCase: IsUserSubscribedUseCase) : ViewModel() {

    private val _isPremiumUser = MutableLiveData<Boolean>()
    val isPremiumUser: LiveData<Boolean> = _isPremiumUser

    fun checkSubscription() {
        _isPremiumUser.value = isUserSubscribedUseCase.execute()
    }
}
