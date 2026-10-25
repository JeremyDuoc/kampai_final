package com.jeremy.kampai

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null

    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, "ca-app-pub-5499036778442602/5835409824", adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadAd(activity)
                }
            }
            rewardedAd?.show(activity) {
                onRewardEarned()
            }
        } else {
            loadAd(activity)
        }
    }
}