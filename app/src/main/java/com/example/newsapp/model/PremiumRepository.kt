import android.content.Context

class PremiumRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PREMIUM_USER = "premium_user"
        private const val KEY_LAST_AD_LOAD_PREFIX = "last_ad_load_" // + fragment name/id
    }

    fun isUserPremium(): Boolean {
        return prefs.getBoolean(KEY_PREMIUM_USER, false)
    }

    fun setUserPremium(isPremium: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM_USER, isPremium).apply()
    }

    fun getLastAdLoadTime(fragmentId: String): Long {
        return prefs.getLong("$KEY_LAST_AD_LOAD_PREFIX$fragmentId", 0L)
    }

    fun setLastAdLoadTime(fragmentId: String, timestamp: Long) {
        prefs.edit().putLong("$KEY_LAST_AD_LOAD_PREFIX$fragmentId", timestamp).apply()
    }
}

class IsUserSubscribedUseCase(private val premiumRepository: PremiumRepository) {
    fun execute(): Boolean = premiumRepository.isUserPremium()
}


